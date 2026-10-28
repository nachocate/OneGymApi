package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.*
import java.time.LocalDate

interface ActivityRepository {
    fun list(gymId: Long, includeInactive: Boolean): List<ActivityResponse>
    fun create(gymId: Long, request: SaveActivityRequest): ActivityResponse
    fun update(gymId: Long, activityId: Long, request: SaveActivityRequest): ActivityResponse
    fun createSchedule(gymId: Long, activityId: Long, request: SaveActivityScheduleRequest): ActivityScheduleResponse
    fun createGroup(gymId: Long, activityId: Long, request: SaveActivityGroupRequest): ActivityGroupResponse
    fun listGroups(gymId: Long, activityId: Long): List<ActivityGroupResponse>
    fun joinOpenGroup(gymId: Long, groupId: Long, userId: Long): ActivityGroupMembershipResponse
    fun requestJoin(gymId: Long, groupId: Long, userId: Long): ActivityJoinRequestResponse
    fun approveRequest(gymId: Long, requestId: Long, reviewerId: Long): ActivityGroupMembershipResponse
    fun activityIdForJoinRequest(gymId: Long, requestId: Long): Long
    fun listMemberships(gymId: Long, groupId: Long): List<ActivityGroupMembershipResponse>
    fun transferMembership(gymId: Long, membershipId: Long, targetGroupId: Long, effectiveFrom: LocalDate): ActivityGroupMembershipResponse
    fun listOccurrences(gymId: Long, from: LocalDate, to: LocalDate, includeInactive: Boolean): List<ActivityOccurrenceResponse>
    fun reserve(gymId: Long, occurrenceId: Long, userId: Long): ActivityReservationResponse
    fun cancelReservation(gymId: Long, occurrenceId: Long, userId: Long)
}
