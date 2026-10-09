-- Applies the exercise ownership and evaluation grouping changes to an
-- existing OneGym database without deleting any rows.

BEGIN;

ALTER TABLE exercises
    ADD COLUMN IF NOT EXISTS id_gym BIGINT,
    ADD COLUMN IF NOT EXISTS is_visible_global BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE exercises
    DROP CONSTRAINT IF EXISTS fk_exercises_gym;

ALTER TABLE exercises
    ADD CONSTRAINT fk_exercises_gym
        FOREIGN KEY (id_gym) REFERENCES gyms(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_exercises_gym
    ON exercises(id_gym);

CREATE TABLE IF NOT EXISTS evaluations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    creation_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE user_tests
    ADD COLUMN IF NOT EXISTS id_evaluation BIGINT;

ALTER TABLE user_tests
    DROP CONSTRAINT IF EXISTS fk_user_tests_evaluation;

ALTER TABLE user_tests
    ADD CONSTRAINT fk_user_tests_evaluation
        FOREIGN KEY (id_evaluation) REFERENCES evaluations(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_user_tests_evaluation
    ON user_tests(id_evaluation);

COMMIT;
