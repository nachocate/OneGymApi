package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val videoUrl: String?,
    val imageUrl: String?,
    val gymId: Long? = null,
    val isVisibleGlobal: Boolean = true
)
