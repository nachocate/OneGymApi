package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRole(val id: Long = 0, val name: String)
