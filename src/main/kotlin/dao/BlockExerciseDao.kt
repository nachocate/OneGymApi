package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.BlockExercises
import java.math.BigDecimal
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class BlockExerciseDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BlockExerciseDao>(BlockExercises)
    var blockId by BlockExercises.block
    var exerciseId by BlockExercises.exercise
    var quantityTypeId by BlockExercises.quantityType
    var position by BlockExercises.position
    var repetitions by BlockExercises.repetitions
    var quantity: BigDecimal? by BlockExercises.quantity
}
