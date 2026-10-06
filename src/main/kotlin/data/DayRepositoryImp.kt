package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.DayDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Day
import com.concatstudio.onegym.respository.DayRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DayRepositoryImp : DayRepository {
    override fun getDays() = transaction(Database.connection) { DayDao.all().map(::toModel) }
    override fun getDayById(id: Long) = transaction(Database.connection) { DayDao.findById(id)?.let(::toModel) }
    override fun updateDay(dayId: Long, day: Day) = transaction(Database.connection) { DayDao.findById(dayId)?.apply { number = day.number; weekId = weekRef(day.weekId) }; Unit }
    override fun createDay(day: Day) = transaction(Database.connection) { DayDao.new { number = day.number; weekId = weekRef(day.weekId) }; Unit }
    override fun deleteDayById(id: Long) = transaction(Database.connection) { DayDao.findById(id)?.delete(); Unit }
}
