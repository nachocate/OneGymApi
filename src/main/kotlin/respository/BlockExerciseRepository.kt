package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.BlockExercise

interface BlockExerciseRepository {
    fun getBlockExercises(): List<BlockExercise>
    fun getBlockExerciseById(id: Long): BlockExercise?
    fun updateBlockExercise(blockExerciseId: Long, blockExercise: BlockExercise)
    fun createBlockExercise(blockExercise: BlockExercise)
    fun deleteBlockExerciseById(id: Long)
}
