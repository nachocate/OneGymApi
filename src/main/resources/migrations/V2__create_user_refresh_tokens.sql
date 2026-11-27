-- Apply this once to existing OneGym databases before deploying the JWT code.
-- Fresh environments receive the same definition from onegym_schema.sql.

CREATE TABLE IF NOT EXISTS user_refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    device_info VARCHAR(100),
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_refresh_tokens_active_user
    ON user_refresh_tokens(user_id)
    WHERE is_revoked = FALSE;

CREATE INDEX IF NOT EXISTS idx_user_refresh_tokens_expires_at
    ON user_refresh_tokens(expires_at);
