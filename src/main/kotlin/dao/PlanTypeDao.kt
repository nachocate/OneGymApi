package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.PlanTypes
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class PlanTypeDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PlanTypeDao>(PlanTypes)
    var name by PlanTypes.name
}
