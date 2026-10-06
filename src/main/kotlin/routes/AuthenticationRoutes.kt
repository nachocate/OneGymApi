package com.concatstudio.onegym.routes

import com.concatstudio.onegym.mappers.toResponse
import com.concatstudio.onegym.model.LoginRequest
import com.concatstudio.onegym.model.AuthenticationResponse
import com.concatstudio.onegym.model.LogoutRequest
import com.concatstudio.onegym.model.RefreshTokenRequest
import com.concatstudio.onegym.respository.RefreshTokenRepository
import com.concatstudio.onegym.respository.UserRepository
import com.concatstudio.onegym.security.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.authenticationRouting() {
    val userRepository: UserRepository by inject()
    val refreshTokenRepository: RefreshTokenRepository by inject()
    val jwtService: JwtService by inject()

    route("/login") {
        post {
            val request = call.receive<LoginRequest>()
            val user = userRepository.getUserByEmail(request.email.trim())

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val authenticated = userRepository.checkPassword(
                userId = user.id,
                password = request.password
            )

            if (authenticated) {
                call.respond(issueTokens(user, request.deviceInfo?.trim()?.take(100), jwtService, refreshTokenRepository))
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

    }

    route("/auth") {
        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val replacementRefreshToken = jwtService.createRefreshToken()
            val userId = refreshTokenRepository.rotate(
                currentTokenHash = jwtService.hashRefreshToken(request.refreshToken),
                replacementTokenHash = jwtService.hashRefreshToken(replacementRefreshToken),
                deviceInfo = request.deviceInfo?.trim()?.take(100),
                replacementExpiresAt = java.time.OffsetDateTime.now().plus(jwtService.settings.refreshTokenTtl)
            ) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            val user = userRepository.getUserById(userId) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            call.respond(
                AuthenticationResponse(
                    user = user.toResponse(),
                    accessToken = jwtService.createAccessToken(user),
                    refreshToken = replacementRefreshToken,
                    expiresIn = jwtService.settings.accessTokenTtl.seconds
                )
            )
        }

        post("/logout") {
            val request = call.receive<LogoutRequest>()
            // Logout is idempotent: do not disclose whether a token existed.
            refreshTokenRepository.revoke(jwtService.hashRefreshToken(request.refreshToken))
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun issueTokens(
    user: com.concatstudio.onegym.model.User,
    deviceInfo: String?,
    jwtService: JwtService,
    refreshTokenRepository: RefreshTokenRepository
): AuthenticationResponse {
    val refreshToken = jwtService.createRefreshToken()
    refreshTokenRepository.create(
        userId = user.id,
        tokenHash = jwtService.hashRefreshToken(refreshToken),
        deviceInfo = deviceInfo,
        expiresAt = java.time.OffsetDateTime.now().plus(jwtService.settings.refreshTokenTtl)
    )
    return AuthenticationResponse(
        user = user.toResponse(),
        accessToken = jwtService.createAccessToken(user),
        refreshToken = refreshToken,
        expiresIn = jwtService.settings.accessTokenTtl.seconds
    )
}
