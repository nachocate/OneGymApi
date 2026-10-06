package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class BlockExercise(
    val id: Long,
    val blockId: Long,
    val exerciseId: Long,
    val quantityTypeId: Long?,
    val position: Int,
    val repetitions: Int?,
    val quantity: Double?
)
