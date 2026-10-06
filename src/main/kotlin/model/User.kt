package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val address: String? = null
)
