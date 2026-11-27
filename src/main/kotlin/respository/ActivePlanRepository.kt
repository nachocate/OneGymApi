package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.ActivePlanResponse

interface ActivePlanRepository {
    fun getActivePlans(userId: Long, gymId: Long): List<ActivePlanResponse>
}
