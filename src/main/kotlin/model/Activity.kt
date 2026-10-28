package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable enum class ActivityParticipationMode { OPEN, GROUP_ONLY, HYBRID }
@Serializable enum class ActivityStatus { DRAFT, ACTIVE, SUSPENDED, ARCHIVED }
@Serializable enum class ActivityGroupJoinPolicy { OPEN, REQUEST_ONLY }
@Serializable enum class ActivityGroupStatus { ACTIVE, PAUSED, ARCHIVED }

@Serializable data class ActivityResponse(val id: Long, val gymId: Long, val title: String, val description: String?, val bannerUrl: String?, val participationMode: ActivityParticipationMode, val status: ActivityStatus, val isVisible: Boolean, val defaultMaxCapacity: Int?)
@Serializable data class SaveActivityRequest(val title: String, val description: String? = null, val bannerUrl: String? = null, val participationMode: ActivityParticipationMode, val status: ActivityStatus = ActivityStatus.DRAFT, val isVisible: Boolean = false, val defaultMaxCapacity: Int? = null)
@Serializable data class SaveActivityScheduleRequest(val dayOfWeek: Int, val startsAt: String, val endsAt: String, val maxCapacity: Int? = null, val activeFrom: String, val activeUntil: String? = null, val instructorGymCoachIds: List<Long>)
@Serializable data class ActivityScheduleResponse(val id: Long, val dayOfWeek: Int, val startsAt: String, val endsAt: String, val maxCapacity: Int?, val activeFrom: String, val activeUntil: String?, val instructorGymCoachIds: List<Long>)
@Serializable data class SaveActivityGroupRequest(val name: String, val joinPolicy: ActivityGroupJoinPolicy, val status: ActivityGroupStatus = ActivityGroupStatus.ACTIVE, val maxMembers: Int? = null, val scheduleIds: List<Long>)
@Serializable data class ActivityGroupResponse(val id: Long, val activityId: Long, val name: String, val joinPolicy: ActivityGroupJoinPolicy, val status: ActivityGroupStatus, val maxMembers: Int?, val scheduleIds: List<Long>, val activeMemberCount: Int)
@Serializable data class ActivityGroupMembershipResponse(val id: Long, val groupId: Long, val userId: Long, val startDate: String, val endDate: String?)
@Serializable data class TransferActivityGroupMembershipRequest(val targetGroupId: Long, val effectiveFrom: String? = null)
@Serializable data class ActivityJoinRequestResponse(val id: Long, val groupId: Long, val userId: Long, val status: String, val requestedAt: String)
@Serializable data class ActivityOccurrenceResponse(val id: Long, val activityId: Long, val scheduleId: Long, val occurrenceDate: String, val startsAt: String, val endsAt: String, val maxCapacity: Int?, val status: String, val groupReservedSeats: Int, val confirmedReservations: Int, val availableSeats: Int?)
@Serializable data class ActivityReservationResponse(val id: Long, val occurrenceId: Long, val userId: Long, val status: String)
