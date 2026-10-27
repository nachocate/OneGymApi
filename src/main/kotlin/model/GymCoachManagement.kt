package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

/** Dashboard-oriented representation: no client-side joins between users and coach types are needed. */
@Serializable
data class GymCoachDetailsResponse(
    val id: Long,
    val user: UserResponse,
    val coachType: CoachType,
    val startDate: String,
    val endDate: String?
)

/** Creates a global user, its COACH membership and its coach assignment atomically. */
@Serializable
data class RegisterGymCoachRequest(
    val user: NewUserRequest,
    val coachTypeId: Long,
    val startDate: String,
    val endDate: String? = null
)

/** Assigns an existing global user as a coach of the gym. */
@Serializable
data class AssignExistingGymCoachRequest(
    val userId: Long,
    val coachTypeId: Long,
    val startDate: String,
    val endDate: String? = null
)

/** Updates coach-specific information; it deliberately does not edit the user's global profile. */
@Serializable
data class UpdateGymCoachRequest(
    val coachTypeId: Long,
    val startDate: String,
    val endDate: String? = null
)
