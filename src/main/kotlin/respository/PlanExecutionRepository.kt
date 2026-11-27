package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.PlanExecutionResponse
import com.concatstudio.onegym.model.PlanExecutionSummaryResponse
import com.concatstudio.onegym.model.StartPlanExecutionResult
import java.time.LocalDate

interface PlanExecutionRepository {
    fun start(gymId: Long, userId: Long, userPlanId: Long, planDayId: Long): StartPlanExecutionResult
    fun get(gymId: Long, executionId: Long): PlanExecutionResponse?
    fun updateItem(gymId: Long, executionId: Long, itemId: Long, completed: Boolean): PlanExecutionResponse
    fun list(gymId: Long, userId: Long, from: LocalDate, to: LocalDate): List<PlanExecutionResponse>
    fun summaries(gymId: Long, from: LocalDate, to: LocalDate): List<PlanExecutionSummaryResponse>
}
