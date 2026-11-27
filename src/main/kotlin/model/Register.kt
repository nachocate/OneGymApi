package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Register(
    val id: Long = 0,
    val userPlanId: Long,
    val exerciseId: Long,
    val blockId: Long,
    val weight: Double
)
