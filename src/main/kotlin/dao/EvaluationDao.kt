package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Evaluations
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class EvaluationDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EvaluationDao>(Evaluations)
    var name by Evaluations.name
    var description by Evaluations.description
    var creationDate: OffsetDateTime by Evaluations.creationDate
}
