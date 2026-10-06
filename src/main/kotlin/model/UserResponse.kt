package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val address: String?
)
