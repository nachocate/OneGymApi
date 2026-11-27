package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class NewUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val address: String? = null
)

@Serializable
data class GymSubscriptionRequest(
    val startDate: String,
    val expiresAt: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CreateGymRequest(
    val name: String,
    val description: String? = null,
    val localityId: Long,
    val address: String? = null,
    val phone: String? = null,
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val subscription: GymSubscriptionRequest,
    val ownerUserId: Long? = null,
    val owner: NewUserRequest? = null
)

@Serializable
data class AssignGymMemberRequest(
    val userId: Long,
    val roleName: String = "MEMBER"
)

@Serializable
data class RegisterGymMemberRequest(
    val user: NewUserRequest,
    val roleName: String = "MEMBER"
)
