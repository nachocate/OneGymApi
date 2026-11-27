-- Existing rows receive FALSE, matching the default for future rows.
ALTER TABLE current_plan_activities
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT FALSE;
