package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: String? = null
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
    val deviceInfo: String? = null
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class AuthenticationResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
