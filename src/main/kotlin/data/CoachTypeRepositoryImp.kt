package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.CoachTypeDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.CoachType
import com.concatstudio.onegym.respository.CoachTypeRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class CoachTypeRepositoryImp : CoachTypeRepository {
    override fun getCoachTypes() = transaction(Database.connection) {
        CoachTypeDao.all().map { CoachType(it.id.value, it.name) }
    }

    override fun getCoachTypeById(id: Long) = transaction(Database.connection) {
        CoachTypeDao.findById(id)?.let { CoachType(it.id.value, it.name) }
    }

    override fun createCoachType(coachType: CoachType) = transaction(Database.connection) {
        CoachTypeDao.new { name = coachType.name }
        Unit
    }

    override fun updateCoachType(id: Long, coachType: CoachType) = transaction(Database.connection) {
        CoachTypeDao.findById(id)?.name = coachType.name
        Unit
    }

    override fun deleteCoachTypeById(id: Long) = transaction(Database.connection) {
        CoachTypeDao.findById(id)?.delete()
        Unit
    }
}
