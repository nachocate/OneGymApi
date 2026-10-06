package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.UserGyms
import java.time.LocalDate
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class UserGymDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserGymDao>(UserGyms)
    var userId by UserGyms.user
    var gymId by UserGyms.gym
    var roleId by UserGyms.role
    var startDate: LocalDate by UserGyms.startDate
    var endDate: LocalDate? by UserGyms.endDate
}
