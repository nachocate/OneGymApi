package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.WeekDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Week
import com.concatstudio.onegym.respository.WeekRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class WeekRepositoryImp : WeekRepository {
    override fun getWeeks() = transaction(Database.connection) { WeekDao.all().map(::toModel) }
    override fun getWeekById(id: Long) = transaction(Database.connection) { WeekDao.findById(id)?.let(::toModel) }
    override fun updateWeek(weekId: Long, week: Week) = transaction(Database.connection) { WeekDao.findById(weekId)?.apply { number = week.number; planId = planRef(week.planId) }; Unit }
    override fun createWeek(week: Week) = transaction(Database.connection) { WeekDao.new { number = week.number; planId = planRef(week.planId) }; Unit }
    override fun deleteWeekById(id: Long) = transaction(Database.connection) { WeekDao.findById(id)?.delete(); Unit }
}
