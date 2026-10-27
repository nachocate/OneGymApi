BEGIN;

ALTER TABLE plans
    ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;

ALTER TABLE plans
    ADD CONSTRAINT chk_plans_revision_positive CHECK (revision > 0);

COMMIT;
