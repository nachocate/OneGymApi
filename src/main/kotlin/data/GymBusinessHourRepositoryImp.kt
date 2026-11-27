package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.GymBusinessHourDao
import com.concatstudio.onegym.dao.GymDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.GymBusinessHour
import com.concatstudio.onegym.respository.GymBusinessHourRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.*
import java.time.LocalTime

class GymBusinessHourRepositoryImp : GymBusinessHourRepository {
    override fun getGymBusinessHours() = transaction(Database.connection) {
        GymBusinessHourDao.all().map(::toModel)
    }

    override fun getGymBusinessHoursByGymId(gymId: Long, limit: Int, offset: Long) = transaction(Database.connection) {
        GymBusinessHourDao.find { GymBusinessHours.gym eq gymId }.orderBy(GymBusinessHours.id to SortOrder.ASC).drop(offset.toInt()).take(limit).map(::toModel)
    }

    override fun getGymBusinessHourById(id: Long) = transaction(Database.connection) {
        GymBusinessHourDao.findById(id)?.let(::toModel)
    }

    override fun createGymBusinessHour(gymBusinessHour: GymBusinessHour) = transaction(Database.connection) {
        GymBusinessHourDao.new {
            gymId = GymDao.findById(gymBusinessHour.gymId)!!.id
            dayOfWeek = gymBusinessHour.dayOfWeek
            opensAt = LocalTime.parse(gymBusinessHour.opensAt)
            closesAt = LocalTime.parse(gymBusinessHour.closesAt)
        }
        Unit
    }

    override fun updateGymBusinessHour(id: Long, gymBusinessHour: GymBusinessHour) = transaction(Database.connection) {
        GymBusinessHourDao.findById(id)?.apply {
            gymId = GymDao.findById(gymBusinessHour.gymId)!!.id
            dayOfWeek = gymBusinessHour.dayOfWeek
            opensAt = LocalTime.parse(gymBusinessHour.opensAt)
            closesAt = LocalTime.parse(gymBusinessHour.closesAt)
        }
        Unit
    }

    override fun deleteGymBusinessHourById(id: Long) = transaction(Database.connection) {
        GymBusinessHourDao.findById(id)?.delete()
        Unit
    }
}
