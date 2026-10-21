package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveUserPlanRequest(
    val userId: Long,
    val planId: Long
)

@Serializable
data class SaveCurrentPlanActivityRequest(
    val date: String,
    val userPlanId: Long,
    val activeWeekId: Long?,
    val activeDayId: Long?,
    val isActive: Boolean = false
)

@Serializable
data class SaveRegisterRequest(
    val userPlanId: Long,
    val exerciseId: Long,
    val blockId: Long,
    val weight: Double
)

fun SaveUserPlanRequest.toModel() = UserPlan(userId = userId, planId = planId)

fun SaveCurrentPlanActivityRequest.toModel() = CurrentPlanActivity(
    date = date,
    userPlanId = userPlanId,
    activeWeekId = activeWeekId,
    activeDayId = activeDayId,
    isActive = isActive
)

fun SaveRegisterRequest.toModel() = Register(
    userPlanId = userPlanId,
    exerciseId = exerciseId,
    blockId = blockId,
    weight = weight
)
