BEGIN;

CREATE TABLE plan_executions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_plan_id BIGINT NOT NULL REFERENCES user_plans(id),
    plan_day_id BIGINT NOT NULL REFERENCES days(id),
    execution_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_progress_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    total_items INTEGER NOT NULL,
    completed_items INTEGER NOT NULL DEFAULT 0,
    progress_percent INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_plan_executions_user_plan_date UNIQUE (user_plan_id, execution_date),
    CONSTRAINT chk_plan_executions_status CHECK (status IN ('IN_PROGRESS', 'PARTIAL', 'COMPLETED')),
    CONSTRAINT chk_plan_executions_total_items CHECK (total_items > 0),
    CONSTRAINT chk_plan_executions_completed_items CHECK (completed_items BETWEEN 0 AND total_items),
    CONSTRAINT chk_plan_executions_progress_percent CHECK (progress_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_plan_executions_completed_state CHECK ((status = 'COMPLETED') = (completed_items = total_items)),
    CONSTRAINT chk_plan_executions_completed_at CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL))
);

CREATE INDEX idx_plan_executions_date_status ON plan_executions (execution_date, status);

CREATE TABLE plan_execution_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    execution_id BIGINT NOT NULL REFERENCES plan_executions(id) ON DELETE CASCADE,
    source_block_exercise_id BIGINT REFERENCES block_exercises(id) ON DELETE SET NULL,
    block_position INTEGER NOT NULL,
    block_name VARCHAR(150),
    block_laps INTEGER NOT NULL,
    exercise_position INTEGER NOT NULL,
    exercise_id BIGINT REFERENCES exercises(id) ON DELETE SET NULL,
    exercise_name VARCHAR(150) NOT NULL,
    quantity_type_id BIGINT REFERENCES quantity_types(id) ON DELETE SET NULL,
    repetitions INTEGER,
    quantity NUMERIC(10, 2),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    CONSTRAINT chk_plan_execution_items_block_position CHECK (block_position >= 0),
    CONSTRAINT chk_plan_execution_items_block_laps CHECK (block_laps > 0),
    CONSTRAINT chk_plan_execution_items_exercise_position CHECK (exercise_position >= 0),
    CONSTRAINT chk_plan_execution_items_repetitions CHECK (repetitions IS NULL OR repetitions > 0),
    CONSTRAINT chk_plan_execution_items_quantity CHECK (quantity IS NULL OR quantity > 0),
    CONSTRAINT chk_plan_execution_items_completed_at CHECK ((is_completed AND completed_at IS NOT NULL) OR (NOT is_completed AND completed_at IS NULL)),
    CONSTRAINT uq_plan_execution_items_source UNIQUE (execution_id, source_block_exercise_id)
);

CREATE INDEX idx_plan_execution_items_execution ON plan_execution_items (execution_id);

COMMIT;
