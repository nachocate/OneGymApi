package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.AssignPlanificationRequest
import com.concatstudio.onegym.model.ClonePlanificationRequest
import com.concatstudio.onegym.model.PlanificationResponse
import com.concatstudio.onegym.model.SavePlanificationRequest
import com.concatstudio.onegym.model.UserPlan

interface PlanificationRepository {
    fun getStructure(gymId: Long, planId: Long): PlanificationResponse?
    fun create(gymId: Long, request: SavePlanificationRequest): PlanificationResponse
    fun replace(gymId: Long, planId: Long, request: SavePlanificationRequest): PlanificationResponse
    fun clone(gymId: Long, planId: Long, request: ClonePlanificationRequest): PlanificationResponse
    fun assign(gymId: Long, planId: Long, request: AssignPlanificationRequest): UserPlan
    fun getMemberPlans(gymId: Long, userId: Long): List<UserPlan>
}
