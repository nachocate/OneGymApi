package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Registers
import java.math.BigDecimal
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class RegisterDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RegisterDao>(Registers)
    var userPlanId by Registers.userPlan
    var exerciseId by Registers.exercise
    var blockId by Registers.block
    var weight: BigDecimal by Registers.weight
}
