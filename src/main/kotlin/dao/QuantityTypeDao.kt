package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.QuantityTypes
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class QuantityTypeDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<QuantityTypeDao>(QuantityTypes)
    var name by QuantityTypes.name
}
