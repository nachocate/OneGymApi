package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class CoachType(
    val id: Long,
    val name: String
)
