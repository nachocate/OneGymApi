package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class GymHomeResponse(
    val gym: Gym,
    val coaches: List<CoachResponse>
)

@Serializable
data class CoachResponse(
    val user: UserResponse,
    val coachType: CoachType,
    val startDate: String,
    val endDate: String?
)
