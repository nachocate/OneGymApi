package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.GymCoaches
import java.time.LocalDate
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class GymCoachDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<GymCoachDao>(GymCoaches)
    var userId by GymCoaches.user
    var gymId by GymCoaches.gym
    var coachTypeId by GymCoaches.coachType
    var startDate: LocalDate by GymCoaches.startDate
    var endDate: LocalDate? by GymCoaches.endDate
}
