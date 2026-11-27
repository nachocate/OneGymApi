package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.UserGymDao
import com.concatstudio.onegym.dao.UserDao
import com.concatstudio.onegym.dao.UserRoleDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.GymMember
import com.concatstudio.onegym.model.UserRole
import com.concatstudio.onegym.model.UserResponse
import com.concatstudio.onegym.model.UserGym
import com.concatstudio.onegym.respository.UserGymRepository
import java.time.LocalDate
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserGymRepositoryImp : UserGymRepository {
    override fun getUserGyms() = transaction(Database.connection) { UserGymDao.all().map(::toModel) }
    override fun getGymMembersByGymId(gymId: Long, limit: Int, offset: Long) = transaction(Database.connection) {
        UserGymDao.find { UserGyms.gym eq gymId }
            .orderBy(UserGyms.id to SortOrder.ASC)
            .drop(offset.toInt())
            .take(limit)
            .map { membership ->
                val user = requireNotNull(UserDao.findById(membership.userId.value))
                val role = requireNotNull(UserRoleDao.findById(membership.roleId.value))
                GymMember(
                    id = membership.id.value,
                    user = UserResponse(
                        id = user.id.value,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        avatarUrl = user.avatarUrl,
                        phone = user.phone,
                        address = user.address
                    ),
                    role = UserRole(role.id.value, role.name),
                    startDate = membership.startDate.toString(),
                    endDate = membership.endDate?.toString()
                )
            }
    }
    override fun getUserGymById(id: Long) = transaction(Database.connection) { UserGymDao.findById(id)?.let(::toModel) }
    override fun updateUserGym(userGymId: Long, userGym: UserGym) = transaction(Database.connection) { UserGymDao.findById(userGymId)?.apply { userId = userRef(userGym.userId); gymId = gymRef(userGym.gymId); roleId = roleRef(userGym.roleId); startDate = LocalDate.parse(userGym.startDate); endDate = userGym.endDate?.let(LocalDate::parse) }; Unit }
    override fun createUserGym(userGym: UserGym) = transaction(Database.connection) { UserGymDao.new { userId = userRef(userGym.userId); gymId = gymRef(userGym.gymId); roleId = roleRef(userGym.roleId); startDate = LocalDate.parse(userGym.startDate); endDate = userGym.endDate?.let(LocalDate::parse) }; Unit }
    override fun deleteUserGymById(id: Long) = transaction(Database.connection) { UserGymDao.findById(id)?.delete(); Unit }
}
