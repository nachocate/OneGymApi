package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPlan(val id: Long, val userId: Long, val planId: Long)
