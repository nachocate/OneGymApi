package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Register(
    val id: Long,
    val userPlanId: Long,
    val exerciseId: Long,
    val blockId: Long,
    val weight: Double
)
