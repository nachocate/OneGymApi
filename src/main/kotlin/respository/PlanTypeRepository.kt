package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.PlanType

interface PlanTypeRepository {
    fun getPlanTypes(): List<PlanType>
    fun getPlanTypeById(id: Long): PlanType?
    fun updatePlanType(planTypeId: Long, planType: PlanType)
    fun createPlanType(planType: PlanType)
    fun deletePlanTypeById(id: Long)
}
