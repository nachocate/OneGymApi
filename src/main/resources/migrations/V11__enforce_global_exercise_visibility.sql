BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM exercises
        WHERE gym_id IS NULL AND is_visible_global = FALSE
    ) THEN
        RAISE EXCEPTION 'Cannot enforce global exercise visibility: hidden exercises without a gym exist';
    END IF;
END;
$$;

ALTER TABLE exercises
    ADD CONSTRAINT chk_exercises_global_visibility
    CHECK (gym_id IS NOT NULL OR is_visible_global = TRUE);

COMMIT;
