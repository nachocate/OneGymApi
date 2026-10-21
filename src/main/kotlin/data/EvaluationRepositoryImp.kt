package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.EvaluationDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Evaluation
import com.concatstudio.onegym.respository.EvaluationRepository
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.eq

class EvaluationRepositoryImp : EvaluationRepository {
    override fun getEvaluations() = transaction(Database.connection) { EvaluationDao.all().map(::toModel) }
    override fun getEvaluationsByUserId(userId: Long) = transaction(Database.connection) { EvaluationDao.find { Evaluations.user eq userId }.map(::toModel) }
    override fun getEvaluationById(id: Long) = transaction(Database.connection) { EvaluationDao.findById(id)?.let(::toModel) }
    override fun updateEvaluation(evaluationId: Long, evaluation: Evaluation) = transaction(Database.connection) { EvaluationDao.findById(evaluationId)?.apply { userId = userRef(evaluation.userId); name = evaluation.name; description = evaluation.description; creationDate = OffsetDateTime.parse(evaluation.creationDate) }; Unit }
    override fun createEvaluation(evaluation: Evaluation) = transaction(Database.connection) { EvaluationDao.new { userId = userRef(evaluation.userId); name = evaluation.name; description = evaluation.description; creationDate = OffsetDateTime.parse(evaluation.creationDate) }; Unit }
    override fun deleteEvaluationById(id: Long) = transaction(Database.connection) { EvaluationDao.findById(id)?.delete(); Unit }
}
