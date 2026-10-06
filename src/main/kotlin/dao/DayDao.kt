package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Days
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class DayDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DayDao>(Days)
    var number by Days.number
    var weekId by Days.week
}
