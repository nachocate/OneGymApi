package com.concatstudio.onegym.data

import com.concatstudio.onegym.model.*
import com.concatstudio.onegym.respository.ActivityRepository
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.dao.Users
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.*

class ActivityRepositoryImp : ActivityRepository {
    override fun list(gymId: Long, includeInactive: Boolean) = transaction(Database.connection) {
        requireGym(gymId)
        Activities.selectAll().where { (Activities.gym eq gymId) and if (includeInactive) Op.TRUE else ((Activities.status eq "ACTIVE") and (Activities.isVisible eq true)) }
            .orderBy(Activities.title to SortOrder.ASC).map(::activity)
    }

    override fun create(gymId: Long, request: SaveActivityRequest) = transaction(Database.connection) {
        requireGym(gymId); validateActivity(request)
        val id = Activities.insert {
            it[gym] = EntityID(gymId, Gyms); it[title] = request.title.trim(); it[description] = request.description?.trim(); it[bannerUrl] = request.bannerUrl
            it[participationMode] = request.participationMode.name; it[status] = request.status.name; it[isVisible] = request.isVisible; it[defaultMaxCapacity] = request.defaultMaxCapacity
            it[createdAt] = OffsetDateTime.now(); it[updatedAt] = OffsetDateTime.now()
        } get Activities.id
        activity(Activities.selectAll().where { Activities.id eq id }.single())
    }

    override fun update(gymId: Long, activityId: Long, request: SaveActivityRequest) = transaction(Database.connection) {
        activityRow(gymId, activityId); validateActivity(request)
        Activities.update({ Activities.id eq activityId }) {
            it[title] = request.title.trim(); it[description] = request.description?.trim(); it[bannerUrl] = request.bannerUrl; it[participationMode] = request.participationMode.name
            it[status] = request.status.name; it[isVisible] = request.isVisible; it[defaultMaxCapacity] = request.defaultMaxCapacity; it[updatedAt] = OffsetDateTime.now()
        }
        activityRow(gymId, activityId).let(::activity)
    }

    override fun createSchedule(gymId: Long, activityId: Long, request: SaveActivityScheduleRequest) = transaction(Database.connection) {
        activityRow(gymId, activityId); val values = validateSchedule(gymId, request)
        val id = ActivitySchedules.insert { it[activity] = EntityID(activityId, Activities); it[dayOfWeek] = values.dayOfWeek; it[startsAt] = values.startsAt; it[endsAt] = values.endsAt; it[maxCapacity] = request.maxCapacity; it[activeFrom] = values.activeFrom; it[activeUntil] = values.activeUntil } get ActivitySchedules.id
        request.instructorGymCoachIds.distinct().forEach { coachId -> ActivityScheduleInstructors.insert { it[schedule] = id; it[gymCoach] = EntityID(coachId, GymCoaches) } }
        schedule(id)
    }

    override fun listSchedules(gymId: Long, activityId: Long) = transaction(Database.connection) {
        activityRow(gymId, activityId)
        ActivitySchedules.selectAll().where { ActivitySchedules.activity eq activityId }
            .orderBy(ActivitySchedules.dayOfWeek to SortOrder.ASC, ActivitySchedules.startsAt to SortOrder.ASC)
            .map { schedule(it[ActivitySchedules.id]) }
    }

    override fun updateSchedule(gymId: Long, activityId: Long, scheduleId: Long, request: SaveActivityScheduleRequest) = transaction(Database.connection) {
        scheduleRow(gymId, activityId, scheduleId)
        val values = validateSchedule(gymId, request)
        ActivitySchedules.update({ ActivitySchedules.id eq scheduleId }) {
            it[dayOfWeek] = values.dayOfWeek; it[startsAt] = values.startsAt; it[endsAt] = values.endsAt
            it[maxCapacity] = request.maxCapacity; it[activeFrom] = values.activeFrom; it[activeUntil] = values.activeUntil
        }
        ActivityScheduleInstructors.deleteWhere { ActivityScheduleInstructors.schedule eq scheduleId }
        request.instructorGymCoachIds.distinct().forEach { coachId ->
            ActivityScheduleInstructors.insert { it[schedule] = EntityID(scheduleId, ActivitySchedules); it[gymCoach] = EntityID(coachId, GymCoaches) }
        }
        schedule(EntityID(scheduleId, ActivitySchedules))
    }

    override fun deleteSchedule(gymId: Long, activityId: Long, scheduleId: Long) = transaction(Database.connection) {
        scheduleRow(gymId, activityId, scheduleId)
        if (ActivityGroupSchedules.selectAll().where { ActivityGroupSchedules.schedule eq scheduleId }.any()) {
            throw DomainConflictException("The schedule is used by one or more groups")
        }
        if (ActivityOccurrences.selectAll().where { ActivityOccurrences.schedule eq scheduleId }.any()) {
            throw DomainConflictException("The schedule already has generated occurrences and cannot be deleted")
        }
        ActivityScheduleInstructors.deleteWhere { ActivityScheduleInstructors.schedule eq scheduleId }
        ActivitySchedules.deleteWhere { ActivitySchedules.id eq scheduleId }
        Unit
    }

    override fun createGroup(gymId: Long, activityId: Long, request: SaveActivityGroupRequest) = transaction(Database.connection) {
        val row = activityRow(gymId, activityId); require(row[Activities.participationMode] != "OPEN") { "OPEN activities cannot have groups" }
        require(request.name.isNotBlank()) { "Group name is required" }; require(request.scheduleIds.isNotEmpty()) { "A group requires at least one schedule" }; validateCapacity(request.maxMembers)
        request.scheduleIds.distinct().forEach { scheduleId -> require(scheduleBelongsToActivity(scheduleId, activityId)) { "Schedule must belong to the activity" } }
        val id = ActivityGroups.insert { it[activity] = EntityID(activityId, Activities); it[name] = request.name.trim(); it[joinPolicy] = request.joinPolicy.name; it[status] = request.status.name; it[maxMembers] = request.maxMembers } get ActivityGroups.id
        request.scheduleIds.distinct().forEach { scheduleId -> ActivityGroupSchedules.insert { it[group] = id; it[schedule] = EntityID(scheduleId, ActivitySchedules) } }
        group(id)
    }

    override fun listGroups(gymId: Long, activityId: Long) = transaction(Database.connection) { activityRow(gymId, activityId); ActivityGroups.selectAll().where { ActivityGroups.activity eq activityId }.map { group(it[ActivityGroups.id]) } }

    override fun updateGroup(gymId: Long, activityId: Long, groupId: Long, request: SaveActivityGroupRequest) = transaction(Database.connection) {
        val current = groupRow(gymId, groupId)
        require(current[ActivityGroups.activity].value == activityId) { "Activity group does not belong to this activity" }
        validateGroup(gymId, activityId, request)
        val existingScheduleIds = ActivityGroupSchedules.selectAll().where { ActivityGroupSchedules.group eq groupId }.map { it[ActivityGroupSchedules.schedule].value }.toSet()
        val requestedScheduleIds = request.scheduleIds.distinct().toSet()
        if (existingScheduleIds != requestedScheduleIds && ActivityGroupMemberships.selectAll().where { ActivityGroupMemberships.group eq groupId }.any()) {
            throw DomainConflictException("A group with membership history cannot change its schedules")
        }
        val activeMembers = activeMemberCount(groupId)
        if (request.maxMembers != null && request.maxMembers < activeMembers) throw DomainConflictException("The maximum members cannot be lower than the active membership count")
        ActivityGroups.update({ ActivityGroups.id eq groupId }) { it[name] = request.name.trim(); it[joinPolicy] = request.joinPolicy.name; it[status] = request.status.name; it[maxMembers] = request.maxMembers }
        if (existingScheduleIds != requestedScheduleIds) {
            ActivityGroupSchedules.deleteWhere { ActivityGroupSchedules.group eq groupId }
            requestedScheduleIds.forEach { scheduleId -> ActivityGroupSchedules.insert { it[group] = EntityID(groupId, ActivityGroups); it[schedule] = EntityID(scheduleId, ActivitySchedules) } }
        }
        group(EntityID(groupId, ActivityGroups))
    }

    override fun archiveGroup(gymId: Long, activityId: Long, groupId: Long) = transaction(Database.connection) {
        val current = groupRow(gymId, groupId)
        require(current[ActivityGroups.activity].value == activityId) { "Activity group does not belong to this activity" }
        val today = gymLocalDate(gymId)
        ActivityGroups.update({ ActivityGroups.id eq groupId }) { it[status] = ActivityGroupStatus.ARCHIVED.name }
        ActivityGroupMemberships.update({ (ActivityGroupMemberships.group eq groupId) and ActivityGroupMemberships.endDate.isNull() }) { it[endDate] = today }
        ActivityGroupJoinRequests.update({ (ActivityGroupJoinRequests.group eq groupId) and (ActivityGroupJoinRequests.status eq "PENDING") }) { it[status] = "CANCELLED"; it[reviewedAt] = OffsetDateTime.now(); it[reviewedByUser] = null }
        Unit
    }

    override fun listMyActivities(gymId: Long, userId: Long) = transaction(Database.connection) {
        ActivityGroupMemberships.innerJoin(ActivityGroups).innerJoin(Activities).selectAll().where {
            (Activities.gym eq gymId) and (ActivityGroupMemberships.user eq userId) and ActivityGroupMemberships.endDate.isNull() and (ActivityGroups.status eq ActivityGroupStatus.ACTIVE.name)
        }.orderBy(Activities.title to SortOrder.ASC, ActivityGroups.name to SortOrder.ASC).map { row ->
            MemberActivityResponse(
                activity = activity(row),
                group = group(row[ActivityGroups.id]),
                membership = ActivityGroupMembershipResponse(row[ActivityGroupMemberships.id].value, row[ActivityGroups.id].value, userId, row[ActivityGroupMemberships.startDate].toString(), null)
            )
        }
    }

    override fun leaveGroup(gymId: Long, membershipId: Long, userId: Long) = transaction(Database.connection) {
        val membership = ActivityGroupMemberships.innerJoin(ActivityGroups).innerJoin(Activities).selectAll().where {
            (ActivityGroupMemberships.id eq membershipId) and (ActivityGroupMemberships.user eq userId) and (Activities.gym eq gymId)
        }.singleOrNull() ?: throw DomainNotFoundException("Group membership not found")
        require(membership[ActivityGroupMemberships.endDate] == null) { "Group membership is no longer active" }
        ActivityGroupMemberships.update({ ActivityGroupMemberships.id eq membershipId }) { it[endDate] = gymLocalDate(gymId).minusDays(1) }
        Unit
    }

    override fun joinOpenGroup(gymId: Long, groupId: Long, userId: Long) = transaction(Database.connection) {
        val group = groupRow(gymId, groupId); require(group[ActivityGroups.joinPolicy] == "OPEN") { "This group requires approval" }; require(group[ActivityGroups.status] == "ACTIVE") { "Group is not active" }
        require(!isGroupRunning(groupId, gymId)) { "A group cannot be joined while one of its classes is in progress" }
        createMembership(groupId, userId, LocalDate.now())
    }

    override fun requestJoin(gymId: Long, groupId: Long, userId: Long) = transaction(Database.connection) {
        val group = groupRow(gymId, groupId); require(group[ActivityGroups.joinPolicy] == "REQUEST_ONLY") { "This group accepts direct enrollment" }; require(group[ActivityGroups.status] == "ACTIVE") { "Group is not active" }
        val id = ActivityGroupJoinRequests.insert { it[ActivityGroupJoinRequests.group] = EntityID(groupId, ActivityGroups); it[ActivityGroupJoinRequests.user] = EntityID(userId, Users); it[status] = "PENDING"; it[requestedAt] = OffsetDateTime.now(); it[reviewedAt] = null; it[reviewedByUser] = null } get ActivityGroupJoinRequests.id
        joinRequest(ActivityGroupJoinRequests.selectAll().where { ActivityGroupJoinRequests.id eq id }.single())
    }

    override fun approveRequest(gymId: Long, requestId: Long, reviewerId: Long) = transaction(Database.connection) {
        val request = ActivityGroupJoinRequests.selectAll().where { ActivityGroupJoinRequests.id eq requestId }.singleOrNull() ?: throw DomainNotFoundException("Join request not found")
        require(request[ActivityGroupJoinRequests.status] == "PENDING") { "Join request is no longer pending" }
        groupRow(gymId, request[ActivityGroupJoinRequests.group].value)
        val membership = createMembership(request[ActivityGroupJoinRequests.group].value, request[ActivityGroupJoinRequests.user].value, LocalDate.now())
        ActivityGroupJoinRequests.update({ ActivityGroupJoinRequests.id eq requestId }) { it[status] = "APPROVED"; it[reviewedAt] = OffsetDateTime.now(); it[reviewedByUser] = EntityID(reviewerId, Users) }
        membership
    }

    override fun activityIdForJoinRequest(gymId: Long, requestId: Long) = transaction(Database.connection) {
        val request = ActivityGroupJoinRequests.innerJoin(ActivityGroups).innerJoin(Activities).selectAll().where { (ActivityGroupJoinRequests.id eq requestId) and (Activities.gym eq gymId) }.singleOrNull() ?: throw DomainNotFoundException("Join request not found")
        request[Activities.id].value
    }

    override fun listMemberships(gymId: Long, groupId: Long) = transaction(Database.connection) {
        groupRow(gymId, groupId)
        ActivityGroupMemberships.selectAll().where { ActivityGroupMemberships.group eq groupId }.map { ActivityGroupMembershipResponse(it[ActivityGroupMemberships.id].value, groupId, it[ActivityGroupMemberships.user].value, it[ActivityGroupMemberships.startDate].toString(), it[ActivityGroupMemberships.endDate]?.toString()) }
    }

    override fun transferMembership(gymId: Long, membershipId: Long, targetGroupId: Long, effectiveFrom: LocalDate) = transaction(Database.connection) {
        val membership = ActivityGroupMemberships.innerJoin(ActivityGroups).innerJoin(Activities).selectAll().where { (ActivityGroupMemberships.id eq membershipId) and (Activities.gym eq gymId) }.singleOrNull() ?: throw DomainNotFoundException("Group membership not found")
        require(membership[ActivityGroupMemberships.endDate] == null) { "Group membership is not active" }
        val sourceActivity = membership[Activities.id].value
        val target = groupRow(gymId, targetGroupId)
        require(target[ActivityGroups.activity].value == sourceActivity) { "Target group must belong to the same activity" }
        require(target[ActivityGroups.status] == "ACTIVE") { "Target group is not active" }
        ActivityGroupMemberships.update({ ActivityGroupMemberships.id eq membershipId }) { it[endDate] = effectiveFrom.minusDays(1) }
        createMembership(targetGroupId, membership[ActivityGroupMemberships.user].value, effectiveFrom)
    }

    override fun listOccurrences(gymId: Long, from: LocalDate, to: LocalDate, includeInactive: Boolean) = transaction(Database.connection) {
        require(!to.isBefore(from) && !to.isAfter(from.plusDays(90))) { "Date range must be between 0 and 90 days" }; requireGym(gymId)
        generateOccurrences(gymId, from, to)
        val query = ActivityOccurrences.innerJoin(ActivitySchedules).innerJoin(Activities).selectAll().where {
            (Activities.gym eq gymId) and (ActivityOccurrences.occurrenceDate greaterEq from) and (ActivityOccurrences.occurrenceDate lessEq to) and if (includeInactive) Op.TRUE else ((Activities.status eq "ACTIVE") and (Activities.isVisible eq true) and (ActivityOccurrences.status eq "SCHEDULED"))
        }
        query.orderBy(ActivityOccurrences.startsAt to SortOrder.ASC).map(::occurrence)
    }

    override fun reserve(gymId: Long, occurrenceId: Long, userId: Long) = transaction(Database.connection) {
        val row = occurrenceRow(gymId, occurrenceId); require(row[Activities.participationMode] != "GROUP_ONLY") { "This activity only accepts group memberships" }; require(row[ActivityOccurrences.status] == "SCHEDULED") { "Occurrence is not available" }
        val capacity = row[ActivityOccurrences.maxCapacity]
        if (capacity != null) {
            val groupSeats = groupSeatsForOccurrence(row)
            val reservations = ActivityReservations.selectAll().where { (ActivityReservations.occurrence eq occurrenceId) and (ActivityReservations.status eq "CONFIRMED") }.count().toInt()
            if (groupSeats + reservations >= capacity) throw DomainConflictException("There are no available places")
        }
        val id = ActivityReservations.insert { it[occurrence] = EntityID(occurrenceId, ActivityOccurrences); it[user] = EntityID(userId, Users); it[status] = "CONFIRMED"; it[createdAt] = OffsetDateTime.now(); it[cancelledAt] = null } get ActivityReservations.id
        ActivityReservationResponse(id.value, occurrenceId, userId, "CONFIRMED")
    }

    override fun cancelReservation(gymId: Long, occurrenceId: Long, userId: Long) = transaction(Database.connection) {
        occurrenceRow(gymId, occurrenceId)
        val count = ActivityReservations.update({ (ActivityReservations.occurrence eq occurrenceId) and (ActivityReservations.user eq userId) and (ActivityReservations.status eq "CONFIRMED") }) { it[status] = "CANCELLED"; it[cancelledAt] = OffsetDateTime.now() }
        if (count == 0) throw DomainNotFoundException("Confirmed reservation not found")
    }

    private fun createMembership(groupId: Long, userId: Long, start: LocalDate): ActivityGroupMembershipResponse {
        val group = ActivityGroups.selectAll().where { ActivityGroups.id eq groupId }.single()
        val limit = group[ActivityGroups.maxMembers]
        if (limit != null && activeMemberCount(groupId) >= limit) throw DomainConflictException("The group has no available places")
        val id = ActivityGroupMemberships.insert { it[ActivityGroupMemberships.group] = EntityID(groupId, ActivityGroups); it[ActivityGroupMemberships.user] = EntityID(userId, Users); it[startDate] = start; it[endDate] = null } get ActivityGroupMemberships.id
        return ActivityGroupMembershipResponse(id.value, groupId, userId, start.toString(), null)
    }

    private fun generateOccurrences(gymId: Long, from: LocalDate, to: LocalDate) {
        val zone = ZoneId.of(Gyms.selectAll().where { Gyms.id eq gymId }.single()[Gyms.timezone])
        val schedules = ActivitySchedules.innerJoin(Activities).innerJoin(Gyms).selectAll().where { Activities.gym eq gymId }
        schedules.forEach { row ->
            var date = from
            while (!date.isAfter(to)) {
                if (date.dayOfWeek.value == row[ActivitySchedules.dayOfWeek].toInt() && !date.isBefore(row[ActivitySchedules.activeFrom]) && (row[ActivitySchedules.activeUntil] == null || !date.isAfter(row[ActivitySchedules.activeUntil]))) {
                    val exists = ActivityOccurrences.selectAll().where { (ActivityOccurrences.schedule eq row[ActivitySchedules.id]) and (ActivityOccurrences.occurrenceDate eq date) }.any()
                    if (!exists) ActivityOccurrences.insert { it[schedule] = row[ActivitySchedules.id]; it[occurrenceDate] = date; it[startsAt] = ZonedDateTime.of(date, row[ActivitySchedules.startsAt], zone).toOffsetDateTime(); it[endsAt] = ZonedDateTime.of(date, row[ActivitySchedules.endsAt], zone).toOffsetDateTime(); it[maxCapacity] = row[ActivitySchedules.maxCapacity] ?: row[Activities.defaultMaxCapacity]; it[status] = "SCHEDULED" }
                }; date = date.plusDays(1)
            }
        }
    }

    private fun occurrence(row: ResultRow): ActivityOccurrenceResponse { val groupSeats = groupSeatsForOccurrence(row); val booked = ActivityReservations.selectAll().where { (ActivityReservations.occurrence eq row[ActivityOccurrences.id]) and (ActivityReservations.status eq "CONFIRMED") }.count().toInt(); val max = row[ActivityOccurrences.maxCapacity]; return ActivityOccurrenceResponse(row[ActivityOccurrences.id].value, row[Activities.id].value, row[ActivitySchedules.id].value, row[ActivityOccurrences.occurrenceDate].toString(), row[ActivityOccurrences.startsAt].toString(), row[ActivityOccurrences.endsAt].toString(), max, row[ActivityOccurrences.status], groupSeats, booked, max?.minus(groupSeats + booked)?.coerceAtLeast(0)) }
    private fun groupSeatsForOccurrence(row: ResultRow): Int = ActivityGroupSchedules
        .innerJoin(ActivityGroups)
        .innerJoin(ActivityGroupMemberships)
        .selectAll()
        .where {
            (ActivityGroupSchedules.schedule eq row[ActivitySchedules.id]) and
                (ActivityGroupMemberships.startDate lessEq row[ActivityOccurrences.occurrenceDate]) and
                (ActivityGroupMemberships.endDate.isNull() or (ActivityGroupMemberships.endDate greaterEq row[ActivityOccurrences.occurrenceDate]))
        }
        .count()
        .toInt()
    private fun activity(row: ResultRow) = ActivityResponse(row[Activities.id].value, row[Activities.gym].value, row[Activities.title], row[Activities.description], row[Activities.bannerUrl], ActivityParticipationMode.valueOf(row[Activities.participationMode]), ActivityStatus.valueOf(row[Activities.status]), row[Activities.isVisible], row[Activities.defaultMaxCapacity])
    private fun schedule(id: EntityID<Long>): ActivityScheduleResponse { val row = ActivitySchedules.selectAll().where { ActivitySchedules.id eq id }.single(); val instructors = ActivityScheduleInstructors.selectAll().where { ActivityScheduleInstructors.schedule eq id }.map { it[ActivityScheduleInstructors.gymCoach].value }; return ActivityScheduleResponse(id.value, row[ActivitySchedules.dayOfWeek].toInt(), row[ActivitySchedules.startsAt].toString(), row[ActivitySchedules.endsAt].toString(), row[ActivitySchedules.maxCapacity], row[ActivitySchedules.activeFrom].toString(), row[ActivitySchedules.activeUntil]?.toString(), instructors) }
    private fun group(id: EntityID<Long>): ActivityGroupResponse { val row = ActivityGroups.selectAll().where { ActivityGroups.id eq id }.single(); val schedules = ActivityGroupSchedules.selectAll().where { ActivityGroupSchedules.group eq id }.map { it[ActivityGroupSchedules.schedule].value }; return ActivityGroupResponse(id.value, row[ActivityGroups.activity].value, row[ActivityGroups.name], ActivityGroupJoinPolicy.valueOf(row[ActivityGroups.joinPolicy]), ActivityGroupStatus.valueOf(row[ActivityGroups.status]), row[ActivityGroups.maxMembers], schedules, activeMemberCount(id.value)) }
    private fun joinRequest(row: ResultRow) = ActivityJoinRequestResponse(row[ActivityGroupJoinRequests.id].value, row[ActivityGroupJoinRequests.group].value, row[ActivityGroupJoinRequests.user].value, row[ActivityGroupJoinRequests.status], row[ActivityGroupJoinRequests.requestedAt].toString())
    private fun activityRow(gymId: Long, activityId: Long) = Activities.selectAll().where { (Activities.id eq activityId) and (Activities.gym eq gymId) }.singleOrNull() ?: throw DomainNotFoundException("Activity not found")
    private fun groupRow(gymId: Long, groupId: Long): ResultRow { val row = ActivityGroups.innerJoin(Activities).selectAll().where { (ActivityGroups.id eq groupId) and (Activities.gym eq gymId) }.singleOrNull() ?: throw DomainNotFoundException("Activity group not found"); return row }
    private fun occurrenceRow(gymId: Long, id: Long) = ActivityOccurrences.innerJoin(ActivitySchedules).innerJoin(Activities).selectAll().where { (ActivityOccurrences.id eq id) and (Activities.gym eq gymId) }.singleOrNull() ?: throw DomainNotFoundException("Activity occurrence not found")
    private fun requireGym(gymId: Long) { if (!Gyms.selectAll().where { Gyms.id eq gymId }.any()) throw DomainNotFoundException("Gym not found") }
    private fun scheduleBelongsToActivity(id: Long, activityId: Long) = ActivitySchedules.selectAll().where { (ActivitySchedules.id eq id) and (ActivitySchedules.activity eq activityId) }.any()
    private fun activeMemberCount(groupId: Long) = ActivityGroupMemberships.selectAll().where { (ActivityGroupMemberships.group eq groupId) and ActivityGroupMemberships.endDate.isNull() }.count().toInt()
    private fun isGroupRunning(groupId: Long, gymId: Long): Boolean { val now = OffsetDateTime.now(); return ActivityGroupSchedules.innerJoin(ActivitySchedules).innerJoin(Activities).innerJoin(Gyms).selectAll().where { (ActivityGroupSchedules.group eq groupId) and (Activities.gym eq gymId) }.any { schedule -> val zone = ZoneId.of(schedule[Gyms.timezone]); val today = LocalDate.now(zone); val start = ZonedDateTime.of(today, schedule[ActivitySchedules.startsAt], zone).toOffsetDateTime(); val end = ZonedDateTime.of(today, schedule[ActivitySchedules.endsAt], zone).toOffsetDateTime(); !now.isBefore(start) && now.isBefore(end) } }
    private fun validateActivity(r: SaveActivityRequest) { require(r.title.isNotBlank()) { "Title is required" }; validateCapacity(r.defaultMaxCapacity) }
    private fun validateGroup(gymId: Long, activityId: Long, request: SaveActivityGroupRequest) {
        val row = activityRow(gymId, activityId); require(row[Activities.participationMode] != "OPEN") { "OPEN activities cannot have groups" }
        require(request.name.isNotBlank()) { "Group name is required" }; require(request.scheduleIds.isNotEmpty()) { "A group requires at least one schedule" }; validateCapacity(request.maxMembers)
        request.scheduleIds.distinct().forEach { scheduleId -> require(scheduleBelongsToActivity(scheduleId, activityId)) { "Schedule must belong to the activity" } }
    }
    private fun validateSchedule(gymId: Long, request: SaveActivityScheduleRequest): ScheduleValues {
        require(request.instructorGymCoachIds.isNotEmpty()) { "A schedule requires at least one instructor" }
        val day = request.dayOfWeek.toShort(); require(day in 1..7) { "dayOfWeek must be between 1 and 7" }
        val starts = parseTime(request.startsAt); val ends = parseTime(request.endsAt); require(ends > starts) { "endsAt must be after startsAt" }
        val from = parseDate(request.activeFrom); val until = request.activeUntil?.let(::parseDate); require(until == null || !until.isBefore(from)) { "activeUntil cannot precede activeFrom" }
        validateCapacity(request.maxCapacity)
        request.instructorGymCoachIds.distinct().forEach { coachId ->
            val coach = GymCoaches.selectAll().where { GymCoaches.id eq coachId }.singleOrNull() ?: throw DomainNotFoundException("Gym coach not found")
            require(coach[GymCoaches.gym].value == gymId) { "Instructor must belong to the gym" }
        }
        return ScheduleValues(day, starts, ends, from, until)
    }
    private fun validateCapacity(value: Int?) { require(value == null || value in 1..1000) { "Capacity must be between 1 and 1000" } }
    private fun parseDate(value: String) = runCatching { LocalDate.parse(value) }.getOrElse { throw IllegalArgumentException("Invalid date") }
    private fun parseTime(value: String) = runCatching { LocalTime.parse(value) }.getOrElse { throw IllegalArgumentException("Invalid time") }
    private fun scheduleRow(gymId: Long, activityId: Long, scheduleId: Long) = ActivitySchedules.innerJoin(Activities).selectAll().where { (ActivitySchedules.id eq scheduleId) and (ActivitySchedules.activity eq activityId) and (Activities.gym eq gymId) }.singleOrNull() ?: throw DomainNotFoundException("Activity schedule not found")
    private fun gymLocalDate(gymId: Long): LocalDate { val timezone = Gyms.selectAll().where { Gyms.id eq gymId }.singleOrNull()?.get(Gyms.timezone) ?: throw DomainNotFoundException("Gym not found"); return LocalDate.now(ZoneId.of(timezone)) }
    private data class ScheduleValues(val dayOfWeek: Short, val startsAt: LocalTime, val endsAt: LocalTime, val activeFrom: LocalDate, val activeUntil: LocalDate?)
}
