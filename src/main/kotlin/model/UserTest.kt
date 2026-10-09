package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UserTest(
    val id: Long,
    val exerciseId: Long,
    val userId: Long,
    val evaluationId: Long,
    val quantityTypeId: Long?,
    val repetitions: Int?,
    val quantity: Double?,
    val date: String,
    val description: String
)
