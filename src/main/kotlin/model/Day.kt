package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Day(val id: Long = 0, val number: Int, val weekId: Long)
