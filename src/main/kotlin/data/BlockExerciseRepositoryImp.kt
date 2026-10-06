package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.BlockExerciseDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.BlockExercise
import com.concatstudio.onegym.respository.BlockExerciseRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class BlockExerciseRepositoryImp : BlockExerciseRepository {
    override fun getBlockExercises() = transaction(Database.connection) { BlockExerciseDao.all().map(::toModel) }
    override fun getBlockExerciseById(id: Long) = transaction(Database.connection) { BlockExerciseDao.findById(id)?.let(::toModel) }
    override fun updateBlockExercise(blockExerciseId: Long, blockExercise: BlockExercise) = transaction(Database.connection) { BlockExerciseDao.findById(blockExerciseId)?.apply { blockId = blockRef(blockExercise.blockId); exerciseId = exerciseRef(blockExercise.exerciseId); quantityTypeId = blockExercise.quantityTypeId?.let(::quantityTypeRef); position = blockExercise.position; repetitions = blockExercise.repetitions; quantity = blockExercise.quantity?.toBigDecimal() }; Unit }
    override fun createBlockExercise(blockExercise: BlockExercise) = transaction(Database.connection) { BlockExerciseDao.new { blockId = blockRef(blockExercise.blockId); exerciseId = exerciseRef(blockExercise.exerciseId); quantityTypeId = blockExercise.quantityTypeId?.let(::quantityTypeRef); position = blockExercise.position; repetitions = blockExercise.repetitions; quantity = blockExercise.quantity?.toBigDecimal() }; Unit }
    override fun deleteBlockExerciseById(id: Long) = transaction(Database.connection) { BlockExerciseDao.findById(id)?.delete(); Unit }
}
