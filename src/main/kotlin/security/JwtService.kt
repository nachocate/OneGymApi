package com.concatstudio.onegym.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.concatstudio.onegym.model.User
import com.typesafe.config.ConfigFactory
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class JwtSettings(
    val secret: String,
    val refreshTokenHashSecret: String,
    val issuer: String,
    val audience: String,
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration
) {
    companion object {
        fun load(): JwtSettings {
            val config = ConfigFactory.load().getConfig("jwt")
            return JwtSettings(
                secret = requiredSecret("JWT_SECRET", config.getString("secret")),
                refreshTokenHashSecret = requiredSecret(
                    "JWT_REFRESH_TOKEN_HASH_SECRET",
                    config.getString("refresh-token-hash-secret")
                ),
                issuer = environment("JWT_ISSUER") ?: config.getString("issuer"),
                audience = environment("JWT_AUDIENCE") ?: config.getString("audience"),
                accessTokenTtl = Duration.ofMinutes(
                    (environment("JWT_ACCESS_TOKEN_TTL_MINUTES") ?: config.getLong("access-token-ttl-minutes").toString()).toLong()
                ),
                refreshTokenTtl = Duration.ofDays(
                    (environment("JWT_REFRESH_TOKEN_TTL_DAYS") ?: config.getLong("refresh-token-ttl-days").toString()).toLong()
                )
            )
        }

        private fun requiredSecret(environmentName: String, configuredValue: String): String =
            (environment(environmentName) ?: configuredValue).takeIf { it.length >= 32 }
                ?: error("$environmentName must contain at least 32 characters")

        // Environment variables retain precedence to support a later migration
        // without changing the application code.
        private fun environment(name: String): String? = System.getenv(name) ?: System.getProperty(name)
    }
}

class JwtService(val settings: JwtSettings) {
    private val algorithm = Algorithm.HMAC256(settings.secret)
    private val random = SecureRandom()

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(settings.issuer)
        .withAudience(settings.audience)
        .build()

    fun createAccessToken(user: User): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(settings.issuer)
            .withAudience(settings.audience)
            .withSubject(user.id.toString())
            .withIssuedAt(now)
            .withExpiresAt(now.plus(settings.accessTokenTtl))
            .sign(algorithm)
    }

    fun createRefreshToken(): String = ByteArray(32).also(random::nextBytes).let {
        Base64.getUrlEncoder().withoutPadding().encodeToString(it)
    }

    fun hashRefreshToken(refreshToken: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(settings.refreshTokenHashSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(refreshToken.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
