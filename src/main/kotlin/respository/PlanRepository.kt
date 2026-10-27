package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Plan
import com.concatstudio.onegym.model.PlanSummaryResponse

interface PlanRepository {
    fun getPlans(): List<Plan>
    fun getPlansByGymId(gymId: Long, limit: Int, offset: Long): List<Plan>
    fun getPlanSummariesByGymId(gymId: Long, limit: Int, offset: Long): List<PlanSummaryResponse>
    fun getPlanById(id: Long): Plan?
    fun updatePlan(planId: Long, plan: Plan)
    fun createPlan(plan: Plan)
    fun deletePlanById(id: Long)
}
