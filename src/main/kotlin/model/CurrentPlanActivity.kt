package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class CurrentPlanActivity(
    val id: Long,
    val date: String,
    val userPlanId: Long,
    val activeWeekId: Long?,
    val activeDayId: Long?
)
