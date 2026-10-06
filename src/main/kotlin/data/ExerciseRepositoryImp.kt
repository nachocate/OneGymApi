package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.ExerciseDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Exercise
import com.concatstudio.onegym.respository.ExerciseRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExerciseRepositoryImp : ExerciseRepository {
    override fun getExercises() = transaction(Database.connection) { ExerciseDao.all().map(::toModel) }
    override fun getExerciseById(id: Long) = transaction(Database.connection) { ExerciseDao.findById(id)?.let(::toModel) }
    override fun updateExercise(exerciseId: Long, exercise: Exercise) = transaction(Database.connection) { ExerciseDao.findById(exerciseId)?.apply { name = exercise.name; description = exercise.description; videoUrl = exercise.videoUrl; imageUrl = exercise.imageUrl }; Unit }
    override fun createExercise(exercise: Exercise) = transaction(Database.connection) { ExerciseDao.new { name = exercise.name; description = exercise.description; videoUrl = exercise.videoUrl; imageUrl = exercise.imageUrl }; Unit }
    override fun deleteExerciseById(id: Long) = transaction(Database.connection) { ExerciseDao.findById(id)?.delete(); Unit }
}
