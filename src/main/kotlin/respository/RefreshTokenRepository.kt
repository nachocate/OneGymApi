package com.concatstudio.onegym.respository

import java.time.OffsetDateTime

interface RefreshTokenRepository {
    fun create(userId: Long, tokenHash: String, deviceInfo: String?, expiresAt: OffsetDateTime)

    /**
     * Revokes a valid token and creates its replacement atomically. Returns
     * the owning user id only when the rotation succeeded.
     */
    fun rotate(
        currentTokenHash: String,
        replacementTokenHash: String,
        deviceInfo: String?,
        replacementExpiresAt: OffsetDateTime
    ): Long?

    fun revoke(tokenHash: String): Boolean
}
