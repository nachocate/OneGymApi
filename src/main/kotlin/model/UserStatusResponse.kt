package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStatusResponse(
    val gymCount: Int,
    val gyms: List<GymSubscription>
)

@Serializable
data class GymSubscription(
    val gym: Gym,
    val role: UserRole
)
