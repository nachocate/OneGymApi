package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class GymMember(
    val id: Long,
    val user: UserResponse,
    val role: UserRole,
    val startDate: String,
    val endDate: String?
)
