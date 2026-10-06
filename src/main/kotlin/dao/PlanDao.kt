package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Plans
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class PlanDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PlanDao>(Plans)
    var name by Plans.name
    var gymId by Plans.gym
    var planTypeId by Plans.planType
    var planRootId by Plans.planRoot
}
