package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.UserGymDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.UserGym
import com.concatstudio.onegym.respository.UserGymRepository
import java.time.LocalDate
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserGymRepositoryImp : UserGymRepository {
    override fun getUserGyms() = transaction(Database.connection) { UserGymDao.all().map(::toModel) }
    override fun getUserGymById(id: Long) = transaction(Database.connection) { UserGymDao.findById(id)?.let(::toModel) }
    override fun updateUserGym(userGymId: Long, userGym: UserGym) = transaction(Database.connection) { UserGymDao.findById(userGymId)?.apply { userId = userRef(userGym.userId); gymId = gymRef(userGym.gymId); roleId = roleRef(userGym.roleId); startDate = LocalDate.parse(userGym.startDate); endDate = userGym.endDate?.let(LocalDate::parse) }; Unit }
    override fun createUserGym(userGym: UserGym) = transaction(Database.connection) { UserGymDao.new { userId = userRef(userGym.userId); gymId = gymRef(userGym.gymId); roleId = roleRef(userGym.roleId); startDate = LocalDate.parse(userGym.startDate); endDate = userGym.endDate?.let(LocalDate::parse) }; Unit }
    override fun deleteUserGymById(id: Long) = transaction(Database.connection) { UserGymDao.findById(id)?.delete(); Unit }
}
