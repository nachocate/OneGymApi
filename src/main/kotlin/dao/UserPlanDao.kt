package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.UserPlans
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class UserPlanDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserPlanDao>(UserPlans)
    var userId by UserPlans.user
    var planId by UserPlans.plan
}
