package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UserGym(
    val id: Long = 0,
    val userId: Long,
    val gymId: Long,
    val roleId: Long,
    val startDate: String,
    val endDate: String?
)
