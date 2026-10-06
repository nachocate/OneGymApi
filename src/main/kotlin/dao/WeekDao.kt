package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Weeks
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class WeekDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<WeekDao>(Weeks)
    var number by Weeks.number
    var planId by Weeks.plan
}
