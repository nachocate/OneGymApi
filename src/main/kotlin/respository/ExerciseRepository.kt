package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Exercise

interface ExerciseRepository {
    fun getExercises(): List<Exercise>
    fun getExerciseById(id: Long): Exercise?
    fun updateExercise(exerciseId: Long, exercise: Exercise)
    fun createExercise(exercise: Exercise)
    fun deleteExerciseById(id: Long)
}
