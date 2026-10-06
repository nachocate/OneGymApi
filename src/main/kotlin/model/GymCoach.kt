package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class GymCoach(
    val id: Long,
    val userId: Long,
    val gymId: Long,
    val coachTypeId: Long,
    val startDate: String,
    val endDate: String? = null
)
