-- Enforces invariants that were previously only assumed by the API.
-- Run once, after V8. The migration fails instead of silently changing
-- inconsistent legacy data; repair the reported data before retrying.

BEGIN;

CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users
        GROUP BY lower(btrim(email))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot make emails case-insensitive: duplicate normalized emails exist';
    END IF;

    IF EXISTS (SELECT 1 FROM users WHERE email <> btrim(email) OR btrim(email) = '') THEN
        RAISE EXCEPTION 'Cannot normalize emails: blank or surrounding-whitespace emails exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM user_gyms AS first_membership
        JOIN user_gyms AS second_membership
          ON second_membership.id > first_membership.id
         AND second_membership.id_user = first_membership.id_user
         AND second_membership.id_gym = first_membership.id_gym
         AND daterange(
                first_membership.start_date,
                COALESCE(first_membership.end_date, 'infinity'::date),
                '[]'
             ) && daterange(
                second_membership.start_date,
                COALESCE(second_membership.end_date, 'infinity'::date),
                '[]'
             )
    ) THEN
        RAISE EXCEPTION 'Cannot enforce memberships: overlapping user_gyms periods exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM user_plans
        GROUP BY id_user, id_plan
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot enforce user plans: duplicate user-plan assignments exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM current_plan_activities
        WHERE is_active = TRUE
        GROUP BY id_user_plan
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot enforce active plans: more than one active activity exists for a user plan';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM current_plan_activities AS activity
        JOIN user_plans ON user_plans.id = activity.id_user_plan
        LEFT JOIN weeks AS active_week ON active_week.id = activity.id_week_active
        LEFT JOIN days AS active_day ON active_day.id = activity.id_day_active
        LEFT JOIN weeks AS day_week ON day_week.id = active_day.id_week
        WHERE (activity.id_week_active IS NOT NULL AND active_week.id_plan IS DISTINCT FROM user_plans.id_plan)
           OR (activity.id_day_active IS NOT NULL AND day_week.id_plan IS DISTINCT FROM user_plans.id_plan)
           OR (
                activity.id_week_active IS NOT NULL
                AND activity.id_day_active IS NOT NULL
                AND active_day.id_week IS DISTINCT FROM activity.id_week_active
           )
    ) THEN
        RAISE EXCEPTION 'Cannot enforce active plans: activity hierarchy is inconsistent';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM registers AS register_row
        WHERE NOT EXISTS (
            SELECT 1
            FROM user_plans
            JOIN weeks ON weeks.id_plan = user_plans.id_plan
            JOIN days ON days.id_week = weeks.id
            JOIN blocks ON blocks.id_day = days.id
            JOIN block_exercises
              ON block_exercises.id_block = blocks.id
             AND block_exercises.id_exercise = register_row.id_exercise
            WHERE user_plans.id = register_row.id_user_plan
              AND blocks.id = register_row.id_block
        )
    ) THEN
        RAISE EXCEPTION 'Cannot enforce registers: a block or exercise does not belong to its user plan';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM plans
        JOIN plans AS root_plan ON root_plan.id = plans.id_plan_root
        WHERE root_plan.id_gym <> plans.id_gym
    ) THEN
        RAISE EXCEPTION 'Cannot enforce plan roots: a root belongs to another gym';
    END IF;

    IF EXISTS (
        WITH RECURSIVE ancestry(start_id, current_id, parent_id, path, is_cycle) AS (
            SELECT id, id, id_plan_root, ARRAY[id], FALSE
            FROM plans
            UNION ALL
            SELECT ancestry.start_id,
                   parent.id,
                   parent.id_plan_root,
                   ancestry.path || parent.id,
                   parent.id = ANY(ancestry.path)
            FROM ancestry
            JOIN plans AS parent ON parent.id = ancestry.parent_id
            WHERE NOT ancestry.is_cycle
        )
        SELECT 1 FROM ancestry WHERE is_cycle
    ) THEN
        RAISE EXCEPTION 'Cannot enforce plan roots: a plan-root cycle exists';
    END IF;
END;
$$;

UPDATE users
SET email = lower(btrim(email));

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS uq_users_email,
    ADD CONSTRAINT chk_users_email_normalized
        CHECK (email = btrim(email) AND email <> '');

CREATE UNIQUE INDEX uq_users_email_ci
    ON users (lower(email));

ALTER TABLE user_gyms
    ADD CONSTRAINT ex_user_gyms_no_overlapping_memberships
        EXCLUDE USING gist (
            id_user WITH =,
            id_gym WITH =,
            daterange(start_date, COALESCE(end_date, 'infinity'::date), '[]') WITH &&
        );

ALTER TABLE user_plans
    ADD CONSTRAINT uq_user_plans_user_plan UNIQUE (id_user, id_plan);

CREATE UNIQUE INDEX uq_current_plan_activities_active_user_plan
    ON current_plan_activities(id_user_plan)
    WHERE is_active = TRUE;

CREATE FUNCTION validate_plan_root() RETURNS TRIGGER AS $$
DECLARE
    root_gym_id BIGINT;
    creates_cycle BOOLEAN;
BEGIN
    IF NEW.id_plan_root IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT id_gym INTO root_gym_id FROM plans WHERE id = NEW.id_plan_root;
    IF root_gym_id IS NOT NULL AND root_gym_id <> NEW.id_gym THEN
        RAISE EXCEPTION 'A plan root must belong to the same gym as the plan'
            USING ERRCODE = '23514';
    END IF;

    IF TG_OP = 'UPDATE' THEN
        WITH RECURSIVE ancestors(id, id_plan_root) AS (
            SELECT id, id_plan_root FROM plans WHERE id = NEW.id_plan_root
            UNION
            SELECT plan.id, plan.id_plan_root
            FROM plans AS plan
            JOIN ancestors ON plan.id = ancestors.id_plan_root
        )
        SELECT EXISTS (SELECT 1 FROM ancestors WHERE id = NEW.id)
        INTO creates_cycle;

        IF creates_cycle THEN
            RAISE EXCEPTION 'A plan cannot be an ancestor of itself'
                USING ERRCODE = '23514';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_plan_root
    BEFORE INSERT OR UPDATE OF id_gym, id_plan_root ON plans
    FOR EACH ROW EXECUTE FUNCTION validate_plan_root();

CREATE FUNCTION validate_current_plan_activity_hierarchy() RETURNS TRIGGER AS $$
DECLARE
    expected_plan_id BIGINT;
    active_week_plan_id BIGINT;
    active_day_week_id BIGINT;
    active_day_plan_id BIGINT;
BEGIN
    SELECT id_plan INTO expected_plan_id FROM user_plans WHERE id = NEW.id_user_plan;

    IF NEW.id_week_active IS NOT NULL THEN
        SELECT id_plan INTO active_week_plan_id FROM weeks WHERE id = NEW.id_week_active;
        IF active_week_plan_id IS NOT NULL AND active_week_plan_id <> expected_plan_id THEN
            RAISE EXCEPTION 'The active week must belong to the user plan'
                USING ERRCODE = '23514';
        END IF;
    END IF;

    IF NEW.id_day_active IS NOT NULL THEN
        SELECT days.id_week, weeks.id_plan
        INTO active_day_week_id, active_day_plan_id
        FROM days JOIN weeks ON weeks.id = days.id_week
        WHERE days.id = NEW.id_day_active;

        IF active_day_plan_id IS NOT NULL AND active_day_plan_id <> expected_plan_id THEN
            RAISE EXCEPTION 'The active day must belong to the user plan'
                USING ERRCODE = '23514';
        END IF;

        IF NEW.id_week_active IS NOT NULL AND active_day_week_id <> NEW.id_week_active THEN
            RAISE EXCEPTION 'The active day must belong to the active week'
                USING ERRCODE = '23514';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_current_plan_activity_hierarchy
    BEFORE INSERT OR UPDATE OF id_user_plan, id_week_active, id_day_active ON current_plan_activities
    FOR EACH ROW EXECUTE FUNCTION validate_current_plan_activity_hierarchy();

CREATE FUNCTION validate_register_hierarchy() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM user_plans
        JOIN weeks ON weeks.id_plan = user_plans.id_plan
        JOIN days ON days.id_week = weeks.id
        JOIN blocks ON blocks.id_day = days.id
        JOIN block_exercises
          ON block_exercises.id_block = blocks.id
         AND block_exercises.id_exercise = NEW.id_exercise
        WHERE user_plans.id = NEW.id_user_plan
          AND blocks.id = NEW.id_block
    ) THEN
        RAISE EXCEPTION 'The register block and exercise must belong to the user plan'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_register_hierarchy
    BEFORE INSERT OR UPDATE OF id_user_plan, id_block, id_exercise ON registers
    FOR EACH ROW EXECUTE FUNCTION validate_register_hierarchy();

COMMIT;
