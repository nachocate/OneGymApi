package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class News(
    val id: Long = 0,
    val gymId: Long,
    val title: String,
    val description: String,
    val date: String,
    val imageUrl: String? = null
)
