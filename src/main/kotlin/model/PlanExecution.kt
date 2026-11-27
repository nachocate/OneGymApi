package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class StartPlanExecutionRequest(
    val userPlanId: Long,
    val planDayId: Long
)

@Serializable
data class UpdatePlanExecutionItemRequest(val completed: Boolean)

@Serializable
data class PlanExecutionResponse(
    val id: Long,
    val userPlanId: Long,
    val planId: Long,
    val planDayId: Long,
    val executionDate: String,
    val status: String,
    val startedAt: String,
    val lastProgressAt: String,
    val completedAt: String? = null,
    val totalItems: Int,
    val completedItems: Int,
    val progressPercent: Int,
    val items: List<PlanExecutionItemResponse>
)

@Serializable
data class PlanExecutionItemResponse(
    val id: Long,
    val sourceBlockExerciseId: Long? = null,
    val blockPosition: Int,
    val blockName: String? = null,
    val blockLaps: Int,
    val exercisePosition: Int,
    val exerciseId: Long? = null,
    val exerciseName: String,
    val quantityTypeId: Long? = null,
    val repetitions: Int? = null,
    val quantity: Double? = null,
    val isCompleted: Boolean,
    val completedAt: String? = null
)

@Serializable
data class PlanExecutionSummaryResponse(
    val userId: Long,
    val completedExecutions: Int,
    val partialExecutions: Int,
    val inProgressExecutions: Int,
    val averageProgressPercent: Int
)

data class StartPlanExecutionResult(val execution: PlanExecutionResponse, val created: Boolean)
