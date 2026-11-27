package com.concatstudio.onegym.data

import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.respository.RefreshTokenRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.OffsetDateTime

class RefreshTokenRepositoryImp : RefreshTokenRepository {
    override fun create(userId: Long, tokenHash: String, deviceInfo: String?, expiresAt: OffsetDateTime) {
        transaction(Database.connection) {
            UserRefreshTokens.insert {
                it[user] = userId
                it[UserRefreshTokens.tokenHash] = tokenHash
                it[UserRefreshTokens.deviceInfo] = deviceInfo
                it[UserRefreshTokens.expiresAt] = expiresAt
            }
        }
    }

    override fun rotate(
        currentTokenHash: String,
        replacementTokenHash: String,
        deviceInfo: String?,
        replacementExpiresAt: OffsetDateTime
    ): Long? = transaction(Database.connection) {
        val now = OffsetDateTime.now()
        val token = UserRefreshTokens.selectAll().where {
            UserRefreshTokens.tokenHash eq currentTokenHash
        }.singleOrNull() ?: return@transaction null

        // The conditional update makes simultaneous refresh attempts safe:
        // only one request can revoke the original token and issue a child.
        val revoked = UserRefreshTokens.update({
            (UserRefreshTokens.id eq token[UserRefreshTokens.id]) and
                (UserRefreshTokens.isRevoked eq false) and
                (UserRefreshTokens.expiresAt greater now)
        }) {
            it[isRevoked] = true
        }
        if (revoked != 1) return@transaction null

        UserRefreshTokens.insert {
            it[user] = token[UserRefreshTokens.user]
            it[UserRefreshTokens.tokenHash] = replacementTokenHash
            it[UserRefreshTokens.deviceInfo] = deviceInfo ?: token[UserRefreshTokens.deviceInfo]
            it[UserRefreshTokens.expiresAt] = replacementExpiresAt
        }
        token[UserRefreshTokens.user].value
    }

    override fun revoke(tokenHash: String): Boolean = transaction(Database.connection) {
        UserRefreshTokens.update({
            (UserRefreshTokens.tokenHash eq tokenHash) and (UserRefreshTokens.isRevoked eq false)
        }) { it[isRevoked] = true } == 1
    }
}
