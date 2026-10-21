package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UpdateUserProfileRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val address: String? = null
)
