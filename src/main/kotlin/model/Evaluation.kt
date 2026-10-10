package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Evaluation(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val creationDate: String
)
