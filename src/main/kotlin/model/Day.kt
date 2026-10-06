package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Day(val id: Long, val number: Int, val weekId: Long)
