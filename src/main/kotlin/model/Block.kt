package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Block(
    val id: Long,
    val position: Int,
    val name: String?,
    val dayId: Long,
    val laps: Int
)
