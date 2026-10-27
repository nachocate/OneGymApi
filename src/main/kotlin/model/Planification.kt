package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

/** Complete, editable training-plan tree used by the dashboard planner. */
@Serializable
data class SavePlanificationRequest(
    val name: String,
    val planTypeId: Long,
    val planRootId: Long? = null,
    /** Required by PUT. Send the revision received from GET structure or a previous save. */
    val revision: Long? = null,
    val weeks: List<SavePlanificationWeekRequest>
)

@Serializable
data class SavePlanificationWeekRequest(
    val id: Long? = null,
    val number: Int,
    val days: List<SavePlanificationDayRequest>
)

@Serializable
data class SavePlanificationDayRequest(
    val id: Long? = null,
    val number: Int,
    val blocks: List<SavePlanificationBlockRequest>
)

@Serializable
data class SavePlanificationBlockRequest(
    val id: Long? = null,
    val position: Int,
    val name: String? = null,
    val laps: Int,
    val exercises: List<SavePlanificationBlockExerciseRequest>
)

@Serializable
data class SavePlanificationBlockExerciseRequest(
    val id: Long? = null,
    val exerciseId: Long,
    val quantityTypeId: Long? = null,
    val position: Int,
    val repetitions: Int? = null,
    val quantity: Double? = null
)

@Serializable
data class ClonePlanificationRequest(
    val name: String? = null
)

@Serializable
data class AssignPlanificationRequest(
    val userId: Long
)

@Serializable
data class PlanificationResponse(
    val id: Long,
    val name: String,
    val gymId: Long,
    val planTypeId: Long,
    val planRootId: Long?,
    val revision: Long,
    val weeks: List<PlanificationWeekResponse>
)

@Serializable
data class PlanificationWeekResponse(
    val id: Long,
    val number: Int,
    val days: List<PlanificationDayResponse>
)

@Serializable
data class PlanificationDayResponse(
    val id: Long,
    val number: Int,
    val blocks: List<PlanificationBlockResponse>
)

@Serializable
data class PlanificationBlockResponse(
    val id: Long,
    val position: Int,
    val name: String?,
    val laps: Int,
    val exercises: List<PlanificationBlockExerciseResponse>
)

@Serializable
data class PlanificationBlockExerciseResponse(
    val id: Long,
    val exerciseId: Long,
    val quantityTypeId: Long?,
    val position: Int,
    val repetitions: Int?,
    val quantity: Double?
)
