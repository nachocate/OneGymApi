package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Blocks
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class BlockDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BlockDao>(Blocks)
    var position by Blocks.position
    var name by Blocks.name
    var dayId by Blocks.day
    var laps by Blocks.laps
}
