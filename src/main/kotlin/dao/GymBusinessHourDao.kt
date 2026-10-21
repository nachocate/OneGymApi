package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.GymBusinessHours
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import java.time.LocalTime

class GymBusinessHourDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<GymBusinessHourDao>(GymBusinessHours)

    var gymId by GymBusinessHours.gym
    var dayOfWeek by GymBusinessHours.dayOfWeek
    var opensAt: LocalTime by GymBusinessHours.opensAt
    var closesAt: LocalTime by GymBusinessHours.closesAt
}
