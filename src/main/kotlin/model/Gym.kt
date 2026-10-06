package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class Gym(
    val id: Long,
    val name: String,
    val description: String?,
    val schedule: String?,
    val address: String?,
    val phone: String?,
    val logoUrl: String?,
    val bannerUrl: String?
)
