package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class ActivePlansResponse(
    val plans: List<ActivePlanResponse>
)

@Serializable
data class ActivePlanResponse(
    val id: Long,
    val name: String,
    val isActive: Boolean,
    val weeks: List<ActivePlanWeekResponse>
)

@Serializable
data class ActivePlanWeekResponse(
    val id: Long,
    val number: Int,
    val isActive: Boolean,
    val days: List<ActivePlanDayResponse>
)

@Serializable
data class ActivePlanDayResponse(
    val id: Long,
    val number: Int,
    val isActive: Boolean,
    val blocks: List<ActivePlanBlockResponse>
)

@Serializable
data class ActivePlanBlockResponse(
    val id: Long,
    val position: Int,
    val name: String?,
    val laps: Int,
    val exercises: List<ActivePlanExerciseResponse>
)

@Serializable
data class ActivePlanExerciseResponse(
    /** Identifier of the block_exercises row. */
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val repetitions: Int?,
    val quantity: Double?,
    val quantityType: QuantityType?,
    val position: Int
)
