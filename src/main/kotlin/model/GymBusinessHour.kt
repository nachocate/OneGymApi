package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

/** Informational opening interval. ISO day: 1 = Monday, 7 = Sunday. */
@Serializable
data class GymBusinessHour(
    val id: Long = 0,
    val gymId: Long,
    val dayOfWeek: Short,
    val opensAt: String,
    val closesAt: String
)
