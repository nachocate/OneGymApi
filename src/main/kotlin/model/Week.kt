package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Week(val id: Long = 0, val number: Int, val planId: Long)
