package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.CoachTypeDao
import com.concatstudio.onegym.dao.GymCoachDao
import com.concatstudio.onegym.dao.GymDao
import com.concatstudio.onegym.dao.UserDao
import com.concatstudio.onegym.dao.UserGymDao
import com.concatstudio.onegym.dao.UserRoleDao
import com.concatstudio.onegym.dao.Users
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.AssignExistingGymCoachRequest
import com.concatstudio.onegym.model.CoachType
import com.concatstudio.onegym.model.GymCoachDetailsResponse
import com.concatstudio.onegym.model.NewUserRequest
import com.concatstudio.onegym.model.RegisterGymCoachRequest
import com.concatstudio.onegym.model.UpdateGymCoachRequest
import com.concatstudio.onegym.model.UserResponse
import com.concatstudio.onegym.security.PasswordService
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import java.util.Locale

/**
 * Keeps the dashboard coach workflow transactional: a coach is a user, a COACH
 * membership and a staff assignment. Legacy /gym-coaches CRUD remains available
 * for backwards compatibility, but dashboard routes must use this service.
 */
class GymCoachManagementService {
    fun list(gymId: Long, limit: Int, offset: Long): List<GymCoachDetailsResponse> = transaction(Database.connection) {
        GymCoachDao.find { GymCoaches.gym eq gymId }
            .orderBy(GymCoaches.id to SortOrder.ASC)
            .drop(offset.toInt())
            .take(limit)
            .map(::toDetails)
    }

    fun register(gymId: Long, request: RegisterGymCoachRequest): GymCoachDetailsResponse = transaction(Database.connection) {
        requireGym(gymId)
        val dates = parseDates(request.startDate, request.endDate)
        val user = createUser(request.user)
        ensureCoachMembership(user.id.value, gymId, dates.first)
        createAssignment(user.id.value, gymId, request.coachTypeId, dates.first, dates.second)
    }

    fun assignExisting(gymId: Long, request: AssignExistingGymCoachRequest): GymCoachDetailsResponse = transaction(Database.connection) {
        requireGym(gymId)
        val dates = parseDates(request.startDate, request.endDate)
        if (UserDao.findById(request.userId) == null) throw DomainNotFoundException("User not found")
        ensureCoachMembership(request.userId, gymId, dates.first)
        createAssignment(request.userId, gymId, request.coachTypeId, dates.first, dates.second)
    }

    fun userIdForAssignment(gymId: Long, coachId: Long): Long = transaction(Database.connection) {
        assignmentInGym(gymId, coachId).userId.value
    }

    fun update(gymId: Long, coachId: Long, request: UpdateGymCoachRequest): GymCoachDetailsResponse = transaction(Database.connection) {
        val assignment = assignmentInGym(gymId, coachId)
        val dates = parseDates(request.startDate, request.endDate)
        val coachType = CoachTypeDao.findById(request.coachTypeId) ?: throw DomainNotFoundException("Coach type not found")
        assignment.coachTypeId = coachType.id
        assignment.startDate = dates.first
        assignment.endDate = dates.second
        toDetails(assignment)
    }

    /** Removes staff assignment only. The global user is never deleted. */
    fun delete(gymId: Long, coachId: Long) = transaction(Database.connection) {
        val assignment = assignmentInGym(gymId, coachId)
        val userId = assignment.userId.value
        assignment.delete()
        demoteToMemberWhenNoActiveAssignments(userId, gymId)
        Unit
    }

    private fun createAssignment(
        userId: Long,
        gymId: Long,
        coachTypeId: Long,
        startDate: LocalDate,
        endDate: LocalDate?
    ): GymCoachDetailsResponse {
        val coachType = CoachTypeDao.findById(coachTypeId) ?: throw DomainNotFoundException("Coach type not found")
        val duplicate = GymCoachDao.find {
            (GymCoaches.user eq userId) and
                (GymCoaches.gym eq gymId) and
                (GymCoaches.coachType eq coachType.id) and
                (GymCoaches.startDate eq startDate)
        }.any()
        if (duplicate) throw DomainConflictException("This coach assignment already exists")
        return toDetails(GymCoachDao.new {
            this.userId = EntityID(userId, Users)
            this.gymId = EntityID(gymId, Gyms)
            this.coachTypeId = coachType.id
            this.startDate = startDate
            this.endDate = endDate
        })
    }

    private fun ensureCoachMembership(userId: Long, gymId: Long, startDate: LocalDate) {
        val coachRole = UserRoleDao.find { UserRoles.name eq "COACH" }.firstOrNull()
            ?: throw DomainNotFoundException("Gym role COACH not found")
        val membership = UserGymDao.find {
            (UserGyms.user eq userId) and (UserGyms.gym eq gymId) and UserGyms.endDate.isNull()
        }.firstOrNull()
        if (membership == null) {
            UserGymDao.new {
                this.userId = EntityID(userId, Users)
                this.gymId = EntityID(gymId, Gyms)
                roleId = coachRole.id
                this.startDate = startDate
                endDate = null
            }
            return
        }
        val currentRole = UserRoleDao.findById(membership.roleId.value)
            ?: throw DomainNotFoundException("Gym membership role not found")
        if (currentRole.name == "MEMBER") membership.roleId = coachRole.id
    }

    private fun demoteToMemberWhenNoActiveAssignments(userId: Long, gymId: Long) {
        val today = LocalDate.now()
        val hasActiveAssignment = GymCoachDao.find {
            (GymCoaches.user eq userId) and
                (GymCoaches.gym eq gymId) and
                (GymCoaches.startDate lessEq today) and
                (GymCoaches.endDate.isNull() or (GymCoaches.endDate greaterEq today))
        }.any()
        if (hasActiveAssignment) return
        val membership = UserGymDao.find {
            (UserGyms.user eq userId) and (UserGyms.gym eq gymId) and UserGyms.endDate.isNull()
        }.firstOrNull() ?: return
        val currentRole = UserRoleDao.findById(membership.roleId.value) ?: return
        if (currentRole.name != "COACH") return
        val memberRole = UserRoleDao.find { UserRoles.name eq "MEMBER" }.firstOrNull()
            ?: throw DomainNotFoundException("Gym role MEMBER not found")
        membership.roleId = memberRole.id
    }

    private fun requireGym(gymId: Long) {
        if (GymDao.findById(gymId) == null) throw DomainNotFoundException("Gym not found")
    }

    private fun assignmentInGym(gymId: Long, coachId: Long): GymCoachDao {
        val assignment = GymCoachDao.findById(coachId) ?: throw DomainNotFoundException("Coach assignment not found")
        if (assignment.gymId.value != gymId) throw DomainNotFoundException("Coach assignment not found in this gym")
        return assignment
    }

    private fun parseDates(startDate: String, endDate: String?): Pair<LocalDate, LocalDate?> {
        val start = runCatching { LocalDate.parse(startDate) }
            .getOrElse { throw IllegalArgumentException("Invalid startDate") }
        val end = endDate?.let { runCatching { LocalDate.parse(it) }.getOrElse { throw IllegalArgumentException("Invalid endDate") } }
        require(end == null || !end.isBefore(start)) { "endDate cannot precede startDate" }
        return start to end
    }

    private fun createUser(request: NewUserRequest): UserDao {
        require(request.firstName.isNotBlank()) { "First name is required" }
        require(request.lastName.isNotBlank()) { "Last name is required" }
        require(request.email.isNotBlank()) { "Email is required" }
        require(request.password.isNotBlank()) { "Password is required" }
        val email = request.email.trim().lowercase(Locale.ROOT)
        if (UserDao.find { Users.email eq email }.any()) throw DomainConflictException("A user with this email already exists")
        return UserDao.new {
            firstName = request.firstName.trim()
            lastName = request.lastName.trim()
            this.email = email
            password = PasswordService.encode(request.password)
            avatarUrl = request.avatarUrl
            phone = request.phone
            address = request.address
        }
    }

    private fun toDetails(assignment: GymCoachDao): GymCoachDetailsResponse {
        val user = UserDao.findById(assignment.userId.value) ?: throw DomainNotFoundException("Coach user not found")
        val type = CoachTypeDao.findById(assignment.coachTypeId.value) ?: throw DomainNotFoundException("Coach type not found")
        return GymCoachDetailsResponse(
            id = assignment.id.value,
            user = UserResponse(user.id.value, user.firstName, user.lastName, user.email, user.avatarUrl, user.phone, user.address),
            coachType = CoachType(type.id.value, type.name),
            startDate = assignment.startDate.toString(),
            endDate = assignment.endDate?.toString()
        )
    }
}
