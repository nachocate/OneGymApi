package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.GymDao
import com.concatstudio.onegym.dao.UserDao
import com.concatstudio.onegym.dao.UserGymDao
import com.concatstudio.onegym.dao.Users
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.AssignGymMemberRequest
import com.concatstudio.onegym.model.CreateGymRequest
import com.concatstudio.onegym.model.Gym
import com.concatstudio.onegym.model.NewUserRequest
import com.concatstudio.onegym.model.RegisterGymMemberRequest
import com.concatstudio.onegym.model.UserGym
import com.concatstudio.onegym.security.GymRole
import com.concatstudio.onegym.security.PasswordService
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import java.util.Locale

class DomainNotFoundException(message: String) : IllegalArgumentException(message)
class DomainConflictException(message: String) : IllegalStateException(message)

class GymManagementService {
    fun createGym(request: CreateGymRequest): Gym = transaction(Database.connection) {
        require(request.name.isNotBlank()) { "Gym name is required" }
        require(request.localityId > 0) { "A valid locality is required" }
        require((request.ownerUserId == null) != (request.owner == null)) {
            "Provide exactly one initial owner: ownerUserId or owner"
        }
        require(request.subscription.isActive) { "A newly created gym must start active" }

        val locality = EntityID(request.localityId, Localities)
        if (!Localities.selectAll().where { Localities.id eq request.localityId }.any()) {
            throw DomainNotFoundException("Locality not found")
        }
        val ownerId = request.ownerUserId ?: createUser(request.owner!!).id.value
        if (UserDao.findById(ownerId) == null) throw DomainNotFoundException("Owner user not found")

        val gym = GymDao.new {
            name = request.name.trim()
            description = request.description
            localityId = locality
            address = request.address
            phone = request.phone
            logoUrl = request.logoUrl
            bannerUrl = request.bannerUrl
        }
        val startDate = runCatching { LocalDate.parse(request.subscription.startDate) }
            .getOrElse { throw IllegalArgumentException("Invalid subscription startDate") }
        val expiresAt = request.subscription.expiresAt?.let {
            runCatching { LocalDate.parse(it) }.getOrElse { throw IllegalArgumentException("Invalid subscription expiresAt") }
        }
        require(expiresAt == null || !expiresAt.isBefore(startDate)) {
            "Subscription expiry cannot precede its start date"
        }
        GymSubscriptions.insert {
            it[GymSubscriptions.gym] = gym.id
            it[GymSubscriptions.startDate] = startDate
            it[GymSubscriptions.expiresAt] = expiresAt
            it[GymSubscriptions.isActive] = true
        }
        createMembership(ownerId, gym.id.value, GymRole.OWNER)
        toModel(gym)
    }

    fun assignMember(gymId: Long, request: AssignGymMemberRequest): UserGym = transaction(Database.connection) {
        if (GymDao.findById(gymId) == null) throw DomainNotFoundException("Gym not found")
        if (UserDao.findById(request.userId) == null) throw DomainNotFoundException("User not found")
        createMembership(request.userId, gymId, roleFrom(request.roleName))
    }

    fun registerMember(gymId: Long, request: RegisterGymMemberRequest): UserGym = transaction(Database.connection) {
        if (GymDao.findById(gymId) == null) throw DomainNotFoundException("Gym not found")
        val user = createUser(request.user)
        createMembership(user.id.value, gymId, roleFrom(request.roleName))
    }

    fun removeMember(gymId: Long, userId: Long) = transaction(Database.connection) {
        val membership = UserGymDao.find {
            (UserGyms.gym eq gymId) and (UserGyms.user eq userId)
        }.firstOrNull() ?: throw DomainNotFoundException("Gym membership not found")
        membership.delete()
        Unit
    }

    private fun createMembership(userId: Long, gymId: Long, role: GymRole): UserGym {
        val roleDao = com.concatstudio.onegym.dao.UserRoleDao.find { UserRoles.name eq role.name }.firstOrNull()
            ?: throw DomainNotFoundException("Gym role ${role.name} not found")
        val exists = UserGymDao.find {
            (UserGyms.user eq userId) and (UserGyms.gym eq gymId)
        }.any()
        if (exists) throw DomainConflictException("The user already belongs to this gym")
        return toModel(UserGymDao.new {
            this.userId = EntityID(userId, Users)
            this.gymId = EntityID(gymId, Gyms)
            this.roleId = roleDao.id
            startDate = LocalDate.now()
            endDate = null
        })
    }

    private fun createUser(request: NewUserRequest): UserDao {
        require(request.email.isNotBlank()) { "Email is required" }
        require(request.password.isNotBlank()) { "Password is required" }
        val email = request.email.trim().lowercase(Locale.ROOT)
        if (UserDao.find { Users.email eq email }.any()) {
            throw DomainConflictException("A user with this email already exists")
        }
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

    private fun roleFrom(name: String): GymRole = GymRole.fromName(name)
        ?: throw IllegalArgumentException("Invalid gym role")
}
