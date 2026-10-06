package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.GymCoachDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.GymCoach
import com.concatstudio.onegym.respository.GymCoachRepository
import java.time.LocalDate
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class GymCoachRepositoryImp : GymCoachRepository {
    override fun getGymCoaches() = transaction(Database.connection) { GymCoachDao.all().map(::toModel) }
    override fun getGymCoachById(id: Long) = transaction(Database.connection) { GymCoachDao.findById(id)?.let(::toModel) }

    override fun createGymCoach(gymCoach: GymCoach) = transaction(Database.connection) {
        GymCoachDao.new {
            userId = userRef(gymCoach.userId)
            gymId = gymRef(gymCoach.gymId)
            coachTypeId = coachTypeRef(gymCoach.coachTypeId)
            startDate = LocalDate.parse(gymCoach.startDate)
            endDate = gymCoach.endDate?.let(LocalDate::parse)
        }
        Unit
    }

    override fun updateGymCoach(id: Long, gymCoach: GymCoach) = transaction(Database.connection) {
        GymCoachDao.findById(id)?.apply {
            userId = userRef(gymCoach.userId)
            gymId = gymRef(gymCoach.gymId)
            coachTypeId = coachTypeRef(gymCoach.coachTypeId)
            startDate = LocalDate.parse(gymCoach.startDate)
            endDate = gymCoach.endDate?.let(LocalDate::parse)
        }
        Unit
    }

    override fun deleteGymCoachById(id: Long) = transaction(Database.connection) {
        GymCoachDao.findById(id)?.delete()
        Unit
    }
}
