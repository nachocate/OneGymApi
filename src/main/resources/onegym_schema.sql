-- schema.sql
-- OneGym PostgreSQL schema
-- Entire schema creation runs in a single transaction.

BEGIN;

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

                       CONSTRAINT uq_users_email UNIQUE (email)
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
-- GYMS
-- ============================================================

CREATE TABLE gyms (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      name VARCHAR(150) NOT NULL,
                      description TEXT,
                      schedule TEXT,
                      address VARCHAR(255),
                      phone VARCHAR(50),
                      logo_url TEXT,
                      banner_url TEXT
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
-- USER <-> GYM
-- ============================================================

CREATE TABLE user_gyms (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           id_user BIGINT NOT NULL,
                           id_gym BIGINT NOT NULL,
                           id_user_rol BIGINT NOT NULL,
                           start_date DATE NOT NULL,
                           end_date DATE,

                           CONSTRAINT fk_user_gyms_user
                               FOREIGN KEY (id_user) REFERENCES users(id),

                           CONSTRAINT fk_user_gyms_gym
                               FOREIGN KEY (id_gym) REFERENCES gyms(id),

                           CONSTRAINT fk_user_gyms_role
                               FOREIGN KEY (id_user_rol) REFERENCES user_roles(id),

                           CONSTRAINT chk_user_gyms_dates
                               CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================
-- NEWS
-- ============================================================

CREATE TABLE news (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      id_gym BIGINT NOT NULL,
                      title VARCHAR(255) NOT NULL,
                      description TEXT NOT NULL,
                      date TIMESTAMPTZ NOT NULL,

                      CONSTRAINT fk_news_gym
                          FOREIGN KEY (id_gym) REFERENCES gyms(id)
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
                       id_gym BIGINT NOT NULL,
                       id_plan_type BIGINT NOT NULL,
                       id_plan_root BIGINT,

                       CONSTRAINT fk_plans_gym
                           FOREIGN KEY (id_gym) REFERENCES gyms(id),

                       CONSTRAINT fk_plans_plan_type
                           FOREIGN KEY (id_plan_type) REFERENCES plan_types(id),

                       CONSTRAINT fk_plans_plan_root
                           FOREIGN KEY (id_plan_root) REFERENCES plans(id),

                       CONSTRAINT chk_plans_not_self_root
                           CHECK (id_plan_root IS NULL OR id_plan_root <> id)
);

-- ============================================================
-- WEEKS
-- ============================================================

CREATE TABLE weeks (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       number INTEGER NOT NULL,
                       id_plan BIGINT NOT NULL,

                       CONSTRAINT fk_weeks_plan
                           FOREIGN KEY (id_plan) REFERENCES plans(id),

                       CONSTRAINT chk_weeks_number
                           CHECK (number > 0),

                       CONSTRAINT uq_weeks_plan_number
                           UNIQUE (id_plan, number)
);

-- ============================================================
-- DAYS
-- ============================================================

CREATE TABLE days (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      number INTEGER NOT NULL,
                      id_week BIGINT NOT NULL,

                      CONSTRAINT fk_days_week
                          FOREIGN KEY (id_week) REFERENCES weeks(id),

                      CONSTRAINT chk_days_number
                          CHECK (number > 0),

                      CONSTRAINT uq_days_week_number
                          UNIQUE (id_week, number)
);

-- ============================================================
-- EXERCISES
-- ============================================================

CREATE TABLE exercises (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name VARCHAR(150) NOT NULL,
                           description TEXT,
                           video_url TEXT,
                           image_url TEXT
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
                        id_day BIGINT NOT NULL,
                        laps INTEGER NOT NULL,

                        CONSTRAINT fk_blocks_day
                            FOREIGN KEY (id_day) REFERENCES days(id),

                        CONSTRAINT chk_blocks_position
                            CHECK (position >= 0),

                        CONSTRAINT chk_blocks_laps
                            CHECK (laps > 0),

                        CONSTRAINT uq_blocks_day_position
                            UNIQUE (id_day, position)
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
                                 id_block BIGINT NOT NULL,
                                 id_exercise BIGINT NOT NULL,
                                 id_quantity_type BIGINT,
                                 position INTEGER NOT NULL,
                                 repetitions INTEGER,
                                 quantity NUMERIC(10, 2),

                                 CONSTRAINT fk_block_exercises_block
                                     FOREIGN KEY (id_block) REFERENCES blocks(id),

                                 CONSTRAINT fk_block_exercises_exercise
                                     FOREIGN KEY (id_exercise) REFERENCES exercises(id),

                                 CONSTRAINT fk_block_exercises_quantity_type
                                     FOREIGN KEY (id_quantity_type) REFERENCES quantity_types(id),

                                 CONSTRAINT chk_block_exercises_position
                                     CHECK (position >= 0),

                                 CONSTRAINT chk_block_exercises_repetitions
                                     CHECK (repetitions IS NULL OR repetitions > 0),

                                 CONSTRAINT chk_block_exercises_quantity
                                     CHECK (quantity IS NULL OR quantity > 0),

                                 CONSTRAINT chk_block_exercises_quantity_type
                                     CHECK (
                                         (quantity IS NULL AND id_quantity_type IS NULL)
                                             OR
                                         (quantity IS NOT NULL AND id_quantity_type IS NOT NULL)
                                         ),

                                 CONSTRAINT chk_block_exercises_measurement
                                     CHECK (repetitions IS NOT NULL OR quantity IS NOT NULL),

                                 CONSTRAINT uq_block_exercises_position
                                     UNIQUE (id_block, position)
);

-- ============================================================
-- USER PLANS
-- ============================================================

CREATE TABLE user_plans (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            id_user BIGINT NOT NULL,
                            id_plan BIGINT NOT NULL,

                            CONSTRAINT fk_user_plans_user
                                FOREIGN KEY (id_user) REFERENCES users(id),

                            CONSTRAINT fk_user_plans_plan
                                FOREIGN KEY (id_plan) REFERENCES plans(id)
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
                                         id_user_plan BIGINT NOT NULL,
                                         id_week_active BIGINT,
                                         id_day_active BIGINT,

                                         CONSTRAINT fk_current_plan_activity_user_plan
                                             FOREIGN KEY (id_user_plan) REFERENCES user_plans(id),

                                         CONSTRAINT fk_current_plan_activity_week
                                             FOREIGN KEY (id_week_active) REFERENCES weeks(id),

                                         CONSTRAINT fk_current_plan_activity_day
                                             FOREIGN KEY (id_day_active) REFERENCES days(id)
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
                           id_user_plan BIGINT NOT NULL,
                           id_exercise BIGINT NOT NULL,
                           id_block BIGINT NOT NULL,
                           weight NUMERIC(10, 2) NOT NULL,

                           CONSTRAINT fk_registers_user_plan
                               FOREIGN KEY (id_user_plan) REFERENCES user_plans(id),

                           CONSTRAINT fk_registers_exercise
                               FOREIGN KEY (id_exercise) REFERENCES exercises(id),

                           CONSTRAINT fk_registers_block
                               FOREIGN KEY (id_block) REFERENCES blocks(id),

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

CREATE TABLE user_tests (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            id_exercise BIGINT NOT NULL,
                            id_user BIGINT NOT NULL,
                            id_quantity_type BIGINT,
                            repetitions INTEGER,
                            quantity NUMERIC(10, 2),
                            date TIMESTAMPTZ NOT NULL,
                            description TEXT NOT NULL,

                            CONSTRAINT fk_user_tests_exercise
                                FOREIGN KEY (id_exercise) REFERENCES exercises(id),

                            CONSTRAINT fk_user_tests_user
                                FOREIGN KEY (id_user) REFERENCES users(id),

                            CONSTRAINT fk_user_tests_quantity_type
                                FOREIGN KEY (id_quantity_type) REFERENCES quantity_types(id),

                            CONSTRAINT chk_user_tests_repetitions
                                CHECK (repetitions IS NULL OR repetitions > 0),

                            CONSTRAINT chk_user_tests_quantity
                                CHECK (quantity IS NULL OR quantity > 0),

                            CONSTRAINT chk_user_tests_quantity_type
                                CHECK (
                                    (quantity IS NULL AND id_quantity_type IS NULL)
                                        OR
                                    (quantity IS NOT NULL AND id_quantity_type IS NOT NULL)
                                    ),

                            CONSTRAINT chk_user_tests_measurement
                                CHECK (repetitions IS NOT NULL OR quantity IS NOT NULL)
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_user_gyms_user
    ON user_gyms(id_user);

CREATE INDEX idx_user_refresh_tokens_active_user
    ON user_refresh_tokens(user_id)
    WHERE is_revoked = FALSE;

CREATE INDEX idx_user_refresh_tokens_expires_at
    ON user_refresh_tokens(expires_at);

CREATE INDEX idx_user_gyms_gym
    ON user_gyms(id_gym);

CREATE INDEX idx_user_gyms_role
    ON user_gyms(id_user_rol);

CREATE INDEX idx_news_gym
    ON news(id_gym);

CREATE INDEX idx_news_gym_date
    ON news(id_gym, date);

CREATE INDEX idx_plans_gym
    ON plans(id_gym);

CREATE INDEX idx_plans_plan_type
    ON plans(id_plan_type);

CREATE INDEX idx_plans_plan_root
    ON plans(id_plan_root);

CREATE INDEX idx_weeks_plan
    ON weeks(id_plan);

CREATE INDEX idx_days_week
    ON days(id_week);

CREATE INDEX idx_blocks_day
    ON blocks(id_day);

CREATE INDEX idx_block_exercises_block
    ON block_exercises(id_block);

CREATE INDEX idx_block_exercises_exercise
    ON block_exercises(id_exercise);

CREATE INDEX idx_block_exercises_quantity_type
    ON block_exercises(id_quantity_type);

CREATE INDEX idx_user_plans_user
    ON user_plans(id_user);

CREATE INDEX idx_user_plans_plan
    ON user_plans(id_plan);

CREATE INDEX idx_current_plan_activities_user_plan
    ON current_plan_activities(id_user_plan);

CREATE INDEX idx_current_plan_activities_week
    ON current_plan_activities(id_week_active);

CREATE INDEX idx_current_plan_activities_day
    ON current_plan_activities(id_day_active);

CREATE INDEX idx_registers_user_plan
    ON registers(id_user_plan);

CREATE INDEX idx_registers_exercise
    ON registers(id_exercise);

CREATE INDEX idx_registers_block
    ON registers(id_block);

CREATE INDEX idx_user_tests_user
    ON user_tests(id_user);

CREATE INDEX idx_user_tests_exercise
    ON user_tests(id_exercise);

CREATE INDEX idx_user_tests_quantity_type
    ON user_tests(id_quantity_type);

CREATE INDEX idx_user_tests_user_date
    ON user_tests(id_user, date);

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
