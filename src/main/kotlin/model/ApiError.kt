package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(val code: String, val message: String)
