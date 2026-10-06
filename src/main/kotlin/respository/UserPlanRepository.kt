package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.UserPlan

interface UserPlanRepository {
    fun getUserPlans(): List<UserPlan>
    fun getUserPlanById(id: Long): UserPlan?
    fun updateUserPlan(userPlanId: Long, userPlan: UserPlan)
    fun createUserPlan(userPlan: UserPlan)
    fun deleteUserPlanById(id: Long)
}
