-- schema.sql
-- OneGym PostgreSQL schema
-- Entire schema creation runs in a single transaction.
-- For a clean installation, run this schema and then seed_test_data.sql.
-- The files under migrations/ are only for upgrading databases created by
-- earlier schema versions; do not run them after this clean baseline.

BEGIN;

-- Required by the exclusion constraint that prevents overlapping memberships.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       avatar_url TEXT,
                       firstname VARCHAR(100) NOT NULL,
                       lastname VARCHAR(100) NOT NULL,
                       phone VARCHAR(50),
                       address VARCHAR(255),

                       CONSTRAINT chk_users_email_normalized
                           CHECK (email = btrim(email) AND email <> '')
);

CREATE TABLE system_roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT chk_system_roles_name CHECK (name = 'SUPER_ADMIN')
);

CREATE TABLE user_system_roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    system_role_id BIGINT NOT NULL REFERENCES system_roles(id),
    CONSTRAINT uq_user_system_roles UNIQUE (user_id, system_role_id)
);

-- ============================================================
-- REFRESH TOKENS
--
-- token_hash is an HMAC-SHA-256 digest of the opaque token delivered to the
-- client. Never persist a usable refresh token in plaintext.
-- ============================================================

CREATE TABLE user_refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    device_info VARCHAR(100),
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- GEOGRAPHIC CATALOGS
--
-- A locality represents a city, town or other populated place. A gym stores
-- only its locality; the province and country are derived from that catalog.
-- ============================================================

CREATE TABLE countries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    iso_code CHAR(2) NOT NULL,
    CONSTRAINT uq_countries_name UNIQUE (name),
    CONSTRAINT uq_countries_iso_code UNIQUE (iso_code),
    CONSTRAINT chk_countries_iso_code CHECK (iso_code = upper(iso_code))
);

CREATE TABLE provinces (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES countries(id),
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_provinces_country_name UNIQUE (country_id, name)
);

CREATE TABLE localities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    province_id BIGINT NOT NULL REFERENCES provinces(id),
    name VARCHAR(150) NOT NULL,
    CONSTRAINT uq_localities_province_name UNIQUE (province_id, name)
);

-- ============================================================
-- GYMS
-- ============================================================

CREATE TABLE gyms (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      name VARCHAR(150) NOT NULL,
                      description TEXT,
                      locality_id BIGINT NOT NULL REFERENCES localities(id),
                      address VARCHAR(255),
                      phone VARCHAR(50),
                      logo_url TEXT,
                      banner_url TEXT
);

-- Informational opening hours. They are not used to authorize or block any
-- operational action in the API.
CREATE TABLE gym_business_hours (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    gym_id BIGINT NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL,
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    CONSTRAINT uq_gym_business_hours_interval UNIQUE (gym_id, day_of_week, opens_at),
    CONSTRAINT chk_gym_business_hours_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_gym_business_hours_interval CHECK (closes_at > opens_at)
);

CREATE TABLE gym_subscriptions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    gym_id BIGINT NOT NULL REFERENCES gyms(id),
    start_date DATE NOT NULL,
    expires_at DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMPTZ,
    deactivation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_gym_subscriptions_dates
        CHECK (expires_at IS NULL OR expires_at >= start_date),
    CONSTRAINT chk_gym_subscriptions_deactivation
        CHECK (
            (is_active AND deactivated_at IS NULL AND deactivation_reason IS NULL)
            OR (NOT is_active AND deactivated_at IS NOT NULL)
        ),
    CONSTRAINT ex_gym_subscriptions_no_overlap
        EXCLUDE USING gist (
            gym_id WITH =,
            daterange(start_date, COALESCE(expires_at, 'infinity'::date), '[]') WITH &&
        )
);

-- ============================================================
-- USER ROLES
-- ============================================================

CREATE TABLE user_roles (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,

                            CONSTRAINT uq_user_roles_name UNIQUE (name)
);

-- ============================================================
-- COACH TYPES
-- ============================================================

CREATE TABLE coach_types (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,

                             CONSTRAINT uq_coach_types_name UNIQUE (name)
);

-- ============================================================
-- USER <-> GYM
-- ============================================================

CREATE TABLE user_gyms (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           gym_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           start_date DATE NOT NULL,
                           end_date DATE,

                           CONSTRAINT fk_user_gyms_user
                               FOREIGN KEY (user_id) REFERENCES users(id),

                           CONSTRAINT fk_user_gyms_gym
                               FOREIGN KEY (gym_id) REFERENCES gyms(id),

                           CONSTRAINT fk_user_gyms_role
                               FOREIGN KEY (role_id) REFERENCES user_roles(id),

                           CONSTRAINT chk_user_gyms_dates
                               CHECK (end_date IS NULL OR end_date >= start_date),

                           CONSTRAINT ex_user_gyms_no_overlapping_memberships
                               EXCLUDE USING gist (
                                   user_id WITH =,
                                   gym_id WITH =,
                                   daterange(start_date, COALESCE(end_date, 'infinity'::date), '[]') WITH &&
                               )
);

-- ============================================================
-- GYM <-> COACH
--
-- This models the internal staff structure independently from a user's
-- membership role in the gym.
-- ============================================================

CREATE TABLE gym_coaches (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              gym_id BIGINT NOT NULL,
                              coach_type_id BIGINT NOT NULL,
                              start_date DATE NOT NULL,
                              end_date DATE,

                              CONSTRAINT fk_gym_coaches_user
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                              CONSTRAINT fk_gym_coaches_gym
                                  FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE,

                              CONSTRAINT fk_gym_coaches_coach_type
                                  FOREIGN KEY (coach_type_id) REFERENCES coach_types(id),

                              CONSTRAINT chk_gym_coaches_dates
                                  CHECK (end_date IS NULL OR end_date >= start_date),

                              CONSTRAINT uq_gym_coaches_assignment
                                  UNIQUE (user_id, gym_id, coach_type_id, start_date)
);

-- ============================================================
-- NEWS
-- ============================================================

CREATE TABLE news (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      gym_id BIGINT NOT NULL,
                      title VARCHAR(255) NOT NULL,
                      description TEXT NOT NULL,
                      image_url TEXT DEFAULT NULL,
                      date TIMESTAMPTZ NOT NULL,

                      CONSTRAINT fk_news_gym
                          FOREIGN KEY (gym_id) REFERENCES gyms(id)
);

-- ============================================================
-- PLAN TYPES
-- ============================================================

CREATE TABLE plan_types (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,

                            CONSTRAINT uq_plan_types_name UNIQUE (name)
);

-- ============================================================
-- PLANS
-- ============================================================

CREATE TABLE plans (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(150) NOT NULL,
                       gym_id BIGINT NOT NULL,
                       plan_type_id BIGINT NOT NULL,
                       plan_root_id BIGINT,
                       revision BIGINT NOT NULL DEFAULT 1,

                       CONSTRAINT fk_plans_gym
                           FOREIGN KEY (gym_id) REFERENCES gyms(id),

                       CONSTRAINT fk_plans_plan_type
                           FOREIGN KEY (plan_type_id) REFERENCES plan_types(id),

                       CONSTRAINT fk_plans_plan_root
                           FOREIGN KEY (plan_root_id) REFERENCES plans(id),

                       CONSTRAINT chk_plans_not_self_root
                           CHECK (plan_root_id IS NULL OR plan_root_id <> id),

                       CONSTRAINT chk_plans_revision_positive
                           CHECK (revision > 0)
);

-- ============================================================
-- WEEKS
-- ============================================================

CREATE TABLE weeks (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       number INTEGER NOT NULL,
                       plan_id BIGINT NOT NULL,

                       CONSTRAINT fk_weeks_plan
                           FOREIGN KEY (plan_id) REFERENCES plans(id),

                       CONSTRAINT chk_weeks_number
                           CHECK (number > 0),

                       CONSTRAINT uq_weeks_plan_number
                           UNIQUE (plan_id, number)
);

-- ============================================================
-- DAYS
-- ============================================================

CREATE TABLE days (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      number INTEGER NOT NULL,
                      week_id BIGINT NOT NULL,

                      CONSTRAINT fk_days_week
                          FOREIGN KEY (week_id) REFERENCES weeks(id),

                      CONSTRAINT chk_days_number
                          CHECK (number > 0),

                      CONSTRAINT uq_days_week_number
                          UNIQUE (week_id, number)
);

-- ============================================================
-- EXERCISES
-- ============================================================

CREATE TABLE exercises (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name VARCHAR(150) NOT NULL,
                           description TEXT,
                           video_url TEXT,
                           image_url TEXT,
                           gym_id BIGINT,
                           is_visible_global BOOLEAN NOT NULL DEFAULT TRUE,

                           CONSTRAINT fk_exercises_gym
                               FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE SET NULL,

                           CONSTRAINT chk_exercises_global_visibility
                               CHECK (gym_id IS NOT NULL OR is_visible_global = TRUE)
);

-- ============================================================
-- QUANTITY TYPES
-- ============================================================

CREATE TABLE quantity_types (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,

                                CONSTRAINT uq_quantity_types_name UNIQUE (name)
);

-- ============================================================
-- BLOCKS
-- ============================================================

CREATE TABLE blocks (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        position INTEGER NOT NULL,
                        name VARCHAR(150),
                        day_id BIGINT NOT NULL,
                        laps INTEGER NOT NULL,

                        CONSTRAINT fk_blocks_day
                            FOREIGN KEY (day_id) REFERENCES days(id),

                        CONSTRAINT chk_blocks_position
                            CHECK (position >= 0),

                        CONSTRAINT chk_blocks_laps
                            CHECK (laps > 0),

                        CONSTRAINT uq_blocks_day_position
                            UNIQUE (day_id, position)
);

-- ============================================================
-- BLOCK EXERCISES
--
-- Examples:
-- 20 push-ups:
--   repetitions = 20, quantity = NULL
--
-- Run 5 km:
--   repetitions = NULL, quantity = 5, quantity_type = KILOMETERS
--
-- 5 sprints of 100m:
--   repetitions = 5, quantity = 100, quantity_type = METERS
--
-- 10 bench press reps at 60kg:
--   repetitions = 10, quantity = 60, quantity_type = KG
-- ============================================================

CREATE TABLE block_exercises (
                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 block_id BIGINT NOT NULL,
                                 exercise_id BIGINT NOT NULL,
                                 quantity_type_id BIGINT,
                                 position INTEGER NOT NULL,
                                 repetitions INTEGER,
                                 quantity NUMERIC(10, 2),

                                 CONSTRAINT fk_block_exercises_block
                                     FOREIGN KEY (block_id) REFERENCES blocks(id),

                                 CONSTRAINT fk_block_exercises_exercise
                                     FOREIGN KEY (exercise_id) REFERENCES exercises(id),

                                 CONSTRAINT fk_block_exercises_quantity_type
                                     FOREIGN KEY (quantity_type_id) REFERENCES quantity_types(id),

                                 CONSTRAINT chk_block_exercises_position
                                     CHECK (position >= 0),

                                 CONSTRAINT chk_block_exercises_repetitions
                                     CHECK (repetitions IS NULL OR repetitions > 0),

                                 CONSTRAINT chk_block_exercises_quantity
                                     CHECK (quantity IS NULL OR quantity > 0),

                                 CONSTRAINT chk_block_exercises_quantity_type
                                     CHECK (
                                         (quantity IS NULL AND quantity_type_id IS NULL)
                                             OR
                                         (quantity IS NOT NULL AND quantity_type_id IS NOT NULL)
                                         ),

                                 CONSTRAINT chk_block_exercises_measurement
                                     CHECK (repetitions IS NOT NULL OR quantity IS NOT NULL),

                                 CONSTRAINT uq_block_exercises_position
                                     UNIQUE (block_id, position)
);

-- ============================================================
-- USER PLANS
-- ============================================================

CREATE TABLE user_plans (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            plan_id BIGINT NOT NULL,

                            CONSTRAINT fk_user_plans_user
                                FOREIGN KEY (user_id) REFERENCES users(id),

                            CONSTRAINT fk_user_plans_plan
                                FOREIGN KEY (plan_id) REFERENCES plans(id),

                            CONSTRAINT uq_user_plans_user_plan
                                UNIQUE (user_id, plan_id)
);

-- ============================================================
-- CURRENT PLAN ACTIVITIES
--
-- Both week and day are optional intentionally:
-- - week only: UI can show the active week and all its days.
-- - day: UI can focus on the active day.
-- ============================================================

CREATE TABLE current_plan_activities (
                                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         date TIMESTAMPTZ NOT NULL,
                                         user_plan_id BIGINT NOT NULL,
                                         active_week_id BIGINT,
                                         active_day_id BIGINT,
                                         is_active BOOLEAN NOT NULL DEFAULT FALSE,

                                         CONSTRAINT fk_current_plan_activity_user_plan
                                             FOREIGN KEY (user_plan_id) REFERENCES user_plans(id),

                                         CONSTRAINT fk_current_plan_activity_week
                                             FOREIGN KEY (active_week_id) REFERENCES weeks(id),

                                         CONSTRAINT fk_current_plan_activity_day
                                             FOREIGN KEY (active_day_id) REFERENCES days(id)
);

-- ============================================================
-- REGISTERS
--
-- Programmed/suggested weight for an exercise in a user's plan.
-- It is not an execution history, therefore it has no date.
-- Day and week can be inferred from block -> day -> week.
-- ============================================================

CREATE TABLE registers (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           user_plan_id BIGINT NOT NULL,
                           exercise_id BIGINT NOT NULL,
                           block_id BIGINT NOT NULL,
                           weight NUMERIC(10, 2) NOT NULL,

                           CONSTRAINT fk_registers_user_plan
                               FOREIGN KEY (user_plan_id) REFERENCES user_plans(id),

                           CONSTRAINT fk_registers_exercise
                               FOREIGN KEY (exercise_id) REFERENCES exercises(id),

                           CONSTRAINT fk_registers_block
                               FOREIGN KEY (block_id) REFERENCES blocks(id),

                           CONSTRAINT chk_registers_weight
                               CHECK (weight >= 0)
);

-- ============================================================
-- USER TESTS
--
-- Examples:
-- Maximum push-ups:
--   repetitions = 40, quantity = NULL
--
-- Running test:
--   repetitions = NULL, quantity = 5, quantity_type = KILOMETERS
--
-- Strength test:
--   repetitions = 3, quantity = 100, quantity_type = KG
-- ============================================================

CREATE TABLE evaluations (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             user_id BIGINT NOT NULL REFERENCES users(id),
                             name VARCHAR(150) NOT NULL,
                             description TEXT,
                             creation_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- USER TESTS
-- ============================================================

CREATE TABLE user_tests (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            exercise_id BIGINT NOT NULL,
                            user_id BIGINT NOT NULL,
                            evaluation_id BIGINT NOT NULL,
                            quantity_type_id BIGINT,
                            repetitions INTEGER,
                            quantity NUMERIC(10, 2),
                            date TIMESTAMPTZ NOT NULL,
                            description TEXT NOT NULL,

                            CONSTRAINT fk_user_tests_exercise
                                FOREIGN KEY (exercise_id) REFERENCES exercises(id),

                            CONSTRAINT fk_user_tests_user
                                FOREIGN KEY (user_id) REFERENCES users(id),

                            CONSTRAINT fk_user_tests_evaluation
                                FOREIGN KEY (evaluation_id) REFERENCES evaluations(id),

                            CONSTRAINT fk_user_tests_quantity_type
                                FOREIGN KEY (quantity_type_id) REFERENCES quantity_types(id),

                            CONSTRAINT chk_user_tests_repetitions
                                CHECK (repetitions IS NULL OR repetitions > 0),

                            CONSTRAINT chk_user_tests_quantity
                                CHECK (quantity IS NULL OR quantity > 0),

                            CONSTRAINT chk_user_tests_quantity_type
                                CHECK (
                                    (quantity IS NULL AND quantity_type_id IS NULL)
                                        OR
                                    (quantity IS NOT NULL AND quantity_type_id IS NOT NULL)
                                    ),

                            CONSTRAINT chk_user_tests_measurement
                                CHECK (repetitions IS NOT NULL OR quantity IS NOT NULL)
);

-- Cross-table rules cannot be expressed with a simple foreign key. Keep them
-- in PostgreSQL so they hold regardless of the API client that writes data.
CREATE OR REPLACE FUNCTION validate_plan_root() RETURNS TRIGGER AS $$
DECLARE
    root_gym_id BIGINT;
    creates_cycle BOOLEAN;
BEGIN
    IF NEW.plan_root_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT gym_id INTO root_gym_id
    FROM plans
    WHERE id = NEW.plan_root_id;

    IF root_gym_id IS NOT NULL AND root_gym_id <> NEW.gym_id THEN
        RAISE EXCEPTION 'A plan root must belong to the same gym as the plan'
            USING ERRCODE = '23514';
    END IF;

    IF TG_OP = 'UPDATE' THEN
        WITH RECURSIVE ancestors(id, plan_root_id) AS (
            SELECT id, plan_root_id FROM plans WHERE id = NEW.plan_root_id
            UNION
            SELECT plan.id, plan.plan_root_id
            FROM plans AS plan
            JOIN ancestors ON plan.id = ancestors.plan_root_id
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
    BEFORE INSERT OR UPDATE OF gym_id, plan_root_id ON plans
    FOR EACH ROW EXECUTE FUNCTION validate_plan_root();

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

CREATE OR REPLACE FUNCTION validate_current_plan_activity_hierarchy() RETURNS TRIGGER AS $$
DECLARE
    expected_plan_id BIGINT;
    active_week_plan_id BIGINT;
    active_day_week_id BIGINT;
    active_day_plan_id BIGINT;
BEGIN
    SELECT plan_id INTO expected_plan_id
    FROM user_plans
    WHERE id = NEW.user_plan_id;

    IF NEW.active_week_id IS NOT NULL THEN
        SELECT plan_id INTO active_week_plan_id
        FROM weeks
        WHERE id = NEW.active_week_id;

        IF active_week_plan_id IS NOT NULL AND active_week_plan_id <> expected_plan_id THEN
            RAISE EXCEPTION 'The active week must belong to the user plan'
                USING ERRCODE = '23514';
        END IF;
    END IF;

    IF NEW.active_day_id IS NOT NULL THEN
        SELECT days.week_id, weeks.plan_id
        INTO active_day_week_id, active_day_plan_id
        FROM days
        JOIN weeks ON weeks.id = days.week_id
        WHERE days.id = NEW.active_day_id;

        IF active_day_plan_id IS NOT NULL AND active_day_plan_id <> expected_plan_id THEN
            RAISE EXCEPTION 'The active day must belong to the user plan'
                USING ERRCODE = '23514';
        END IF;

        IF NEW.active_week_id IS NOT NULL AND active_day_week_id <> NEW.active_week_id THEN
            RAISE EXCEPTION 'The active day must belong to the active week'
                USING ERRCODE = '23514';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_current_plan_activity_hierarchy
    BEFORE INSERT OR UPDATE OF user_plan_id, active_week_id, active_day_id ON current_plan_activities
    FOR EACH ROW EXECUTE FUNCTION validate_current_plan_activity_hierarchy();

CREATE OR REPLACE FUNCTION validate_register_hierarchy() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM user_plans
        JOIN weeks ON weeks.plan_id = user_plans.plan_id
        JOIN days ON days.week_id = weeks.id
        JOIN blocks ON blocks.day_id = days.id
        JOIN block_exercises
            ON block_exercises.block_id = blocks.id
           AND block_exercises.exercise_id = NEW.exercise_id
        WHERE user_plans.id = NEW.user_plan_id
          AND blocks.id = NEW.block_id
    ) THEN
        RAISE EXCEPTION 'The register block and exercise must belong to the user plan'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_user_test_evaluation_user() RETURNS TRIGGER AS $$
DECLARE
    evaluation_user_id BIGINT;
BEGIN
    SELECT user_id INTO evaluation_user_id FROM evaluations WHERE id = NEW.evaluation_id;
    IF evaluation_user_id IS NULL OR evaluation_user_id <> NEW.user_id THEN
        RAISE EXCEPTION 'A user test must belong to an evaluation of the same user'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_user_test_evaluation_user
    BEFORE INSERT OR UPDATE OF user_id, evaluation_id ON user_tests
    FOR EACH ROW EXECUTE FUNCTION validate_user_test_evaluation_user();

CREATE TRIGGER trg_validate_register_hierarchy
    BEFORE INSERT OR UPDATE OF user_plan_id, block_id, exercise_id ON registers
    FOR EACH ROW EXECUTE FUNCTION validate_register_hierarchy();

-- Deferred checks allow creating a gym and its first owner, or replacing an
-- owner, in the same transaction while forbidding an ownerless gym at commit.
CREATE OR REPLACE FUNCTION validate_gym_has_owner() RETURNS TRIGGER AS $$
DECLARE
    affected_gym_id BIGINT;
BEGIN
    IF TG_OP = 'DELETE' THEN
        affected_gym_id := OLD.gym_id;
    ELSE
        affected_gym_id := NEW.gym_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM user_gyms
        JOIN user_roles ON user_roles.id = user_gyms.role_id
        WHERE user_gyms.gym_id = affected_gym_id
          AND user_roles.name = 'OWNER'
          AND user_gyms.end_date IS NULL
    ) THEN
        RAISE EXCEPTION 'A gym must retain at least one active owner'
            USING ERRCODE = '23514';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_new_gym_has_owner() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM user_gyms
        JOIN user_roles ON user_roles.id = user_gyms.role_id
        WHERE user_gyms.gym_id = NEW.id
          AND user_roles.name = 'OWNER'
          AND user_gyms.end_date IS NULL
    ) THEN
        RAISE EXCEPTION 'A gym must retain at least one active owner'
            USING ERRCODE = '23514';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_gym_requires_owner
    AFTER INSERT ON gyms DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION validate_new_gym_has_owner();

CREATE CONSTRAINT TRIGGER trg_membership_preserves_gym_owner
    AFTER INSERT OR UPDATE OF gym_id, role_id, end_date OR DELETE ON user_gyms
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION validate_gym_has_owner();

-- ============================================================
-- INDEXES
-- ============================================================

CREATE UNIQUE INDEX uq_users_email_ci
    ON users (lower(email));

CREATE INDEX idx_user_gyms_user_id
    ON user_gyms(user_id);

CREATE INDEX idx_provinces_country_id
    ON provinces(country_id);

CREATE INDEX idx_localities_province_id
    ON localities(province_id);

CREATE INDEX idx_gyms_locality_id
    ON gyms(locality_id);

CREATE INDEX idx_gym_business_hours_gym_id
    ON gym_business_hours(gym_id);

CREATE INDEX idx_user_refresh_tokens_active_user
    ON user_refresh_tokens(user_id)
    WHERE is_revoked = FALSE;

CREATE INDEX idx_user_refresh_tokens_expires_at
    ON user_refresh_tokens(expires_at);

CREATE INDEX idx_user_system_roles_user_id
    ON user_system_roles(user_id);

CREATE INDEX idx_gym_subscriptions_gym_id
    ON gym_subscriptions(gym_id);

CREATE INDEX idx_user_gyms_gym_id
    ON user_gyms(gym_id);

CREATE INDEX idx_user_gyms_role_id
    ON user_gyms(role_id);

CREATE INDEX idx_gym_coaches_gym_id
    ON gym_coaches(gym_id);

CREATE INDEX idx_gym_coaches_user_id
    ON gym_coaches(user_id);

CREATE INDEX idx_gym_coaches_coach_type_id
    ON gym_coaches(coach_type_id);

CREATE INDEX idx_news_gym_id_date
    ON news(gym_id, date);

CREATE INDEX idx_plans_gym_id
    ON plans(gym_id);

CREATE INDEX idx_plans_plan_type_id
    ON plans(plan_type_id);

CREATE INDEX idx_plans_plan_root_id
    ON plans(plan_root_id);

CREATE INDEX idx_exercises_gym_id
    ON exercises(gym_id);

CREATE INDEX idx_block_exercises_exercise_id
    ON block_exercises(exercise_id);

CREATE INDEX idx_block_exercises_quantity_type_id
    ON block_exercises(quantity_type_id);

CREATE INDEX idx_user_plans_plan_id
    ON user_plans(plan_id);

CREATE INDEX idx_current_plan_activities_user_plan_id
    ON current_plan_activities(user_plan_id);

CREATE INDEX idx_current_plan_activities_active_week_id
    ON current_plan_activities(active_week_id);

CREATE INDEX idx_current_plan_activities_active_day_id
    ON current_plan_activities(active_day_id);

CREATE UNIQUE INDEX uq_current_plan_activities_active_user_plan
    ON current_plan_activities(user_plan_id)
    WHERE is_active = TRUE;

CREATE INDEX idx_registers_user_plan_id
    ON registers(user_plan_id);

CREATE INDEX idx_registers_exercise_id
    ON registers(exercise_id);

CREATE INDEX idx_registers_block_id
    ON registers(block_id);

CREATE INDEX idx_user_tests_exercise_id
    ON user_tests(exercise_id);

CREATE INDEX idx_evaluations_user_id
    ON evaluations(user_id);

CREATE INDEX idx_user_tests_evaluation_id
    ON user_tests(evaluation_id);

CREATE INDEX idx_user_tests_quantity_type_id
    ON user_tests(quantity_type_id);

CREATE INDEX idx_user_tests_user_id_date
    ON user_tests(user_id, date);

-- ============================================================
-- INITIAL DATA
-- ============================================================

INSERT INTO quantity_types (name) VALUES
                                      ('SECONDS'),
                                      ('MINUTES'),
                                      ('METERS'),
                                      ('KILOMETERS'),
                                      ('KG');

COMMIT;
