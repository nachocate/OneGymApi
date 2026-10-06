package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Plan

interface PlanRepository {
    fun getPlans(): List<Plan>
    fun getPlanById(id: Long): Plan?
    fun updatePlan(planId: Long, plan: Plan)
    fun createPlan(plan: Plan)
    fun deletePlanById(id: Long)
}
