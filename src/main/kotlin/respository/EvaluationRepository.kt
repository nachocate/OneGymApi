package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Evaluation

interface EvaluationRepository {
    fun getEvaluations(): List<Evaluation>
    fun getEvaluationById(id: Long): Evaluation?
    fun updateEvaluation(evaluationId: Long, evaluation: Evaluation)
    fun createEvaluation(evaluation: Evaluation)
    fun deleteEvaluationById(id: Long)
}
