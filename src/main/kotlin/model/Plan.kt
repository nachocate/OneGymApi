package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Plan(
    val id: Long = 0,
    val name: String,
    val gymId: Long,
    val planTypeId: Long,
    val planRootId: Long?
)
