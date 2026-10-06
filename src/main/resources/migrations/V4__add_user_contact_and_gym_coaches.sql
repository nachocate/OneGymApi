-- Apply once to existing OneGym databases before using gym coaches.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS address VARCHAR(255);

CREATE TABLE IF NOT EXISTS coach_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS gym_coaches (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_user BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_gym BIGINT NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    id_coach_type BIGINT NOT NULL REFERENCES coach_types(id),
    start_date DATE NOT NULL,
    end_date DATE,
    CONSTRAINT chk_gym_coaches_dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT uq_gym_coaches_assignment UNIQUE (id_user, id_gym, id_coach_type, start_date)
);

INSERT INTO coach_types (name) VALUES
    ('headCoach'),
    ('coach'),
    ('assistant')
ON CONFLICT (name) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_gym_coaches_gym ON gym_coaches(id_gym);
CREATE INDEX IF NOT EXISTS idx_gym_coaches_user ON gym_coaches(id_user);
CREATE INDEX IF NOT EXISTS idx_gym_coaches_coach_type ON gym_coaches(id_coach_type);
