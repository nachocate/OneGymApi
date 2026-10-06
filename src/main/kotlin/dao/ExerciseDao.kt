package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Exercises
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ExerciseDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ExerciseDao>(Exercises)
    var name by Exercises.name
    var description by Exercises.description
    var videoUrl by Exercises.videoUrl
    var imageUrl by Exercises.imageUrl
}
