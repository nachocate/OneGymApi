-- A plan can only be assigned to a user who currently belongs to the plan gym.
-- Repair legacy assignments that do not meet this rule before applying the migration.
BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM user_plans
        JOIN plans ON plans.id = user_plans.plan_id
        WHERE NOT EXISTS (
            SELECT 1
            FROM user_gyms
            WHERE user_gyms.user_id = user_plans.user_id
              AND user_gyms.gym_id = plans.gym_id
              AND user_gyms.start_date <= CURRENT_DATE
              AND (user_gyms.end_date IS NULL OR user_gyms.end_date >= CURRENT_DATE)
        )
    ) THEN
        RAISE EXCEPTION 'Cannot enforce user plan membership: assignments without an active gym membership exist';
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION validate_user_plan_membership() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM plans
        JOIN user_gyms ON user_gyms.gym_id = plans.gym_id
        WHERE plans.id = NEW.plan_id
          AND user_gyms.user_id = NEW.user_id
          AND user_gyms.start_date <= CURRENT_DATE
          AND (user_gyms.end_date IS NULL OR user_gyms.end_date >= CURRENT_DATE)
    ) THEN
        RAISE EXCEPTION 'A user plan requires an active membership in the plan gym'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_user_plan_membership
    BEFORE INSERT OR UPDATE OF user_id, plan_id ON user_plans
    FOR EACH ROW EXECUTE FUNCTION validate_user_plan_membership();

COMMIT;
