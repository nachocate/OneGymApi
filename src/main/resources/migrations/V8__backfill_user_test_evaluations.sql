-- Assign every existing user test to an evaluation, then enforce the
-- non-null relationship required by the current domain model.
-- Run after V7__add_exercise_gym_and_evaluations.sql.

BEGIN;

INSERT INTO evaluations (name, description)
SELECT 'Evaluación histórica migrada', 'Agrupa user_tests existentes antes de incorporar evaluaciones.'
WHERE NOT EXISTS (
    SELECT 1
    FROM evaluations
    WHERE name = 'Evaluación histórica migrada'
      AND description = 'Agrupa user_tests existentes antes de incorporar evaluaciones.'
);

UPDATE user_tests
SET id_evaluation = (
    SELECT id
    FROM evaluations
    WHERE name = 'Evaluación histórica migrada'
      AND description = 'Agrupa user_tests existentes antes de incorporar evaluaciones.'
    ORDER BY id
    LIMIT 1
)
WHERE id_evaluation IS NULL;

ALTER TABLE user_tests
    DROP CONSTRAINT IF EXISTS fk_user_tests_evaluation;

ALTER TABLE user_tests
    ALTER COLUMN id_evaluation SET NOT NULL;

ALTER TABLE user_tests
    ADD CONSTRAINT fk_user_tests_evaluation
        FOREIGN KEY (id_evaluation) REFERENCES evaluations(id);

COMMIT;
