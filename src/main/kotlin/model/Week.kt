package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Week(val id: Long, val number: Int, val planId: Long)
