package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.CurrentPlanActivity

interface CurrentPlanActivityRepository {
    fun getCurrentPlanActivities(): List<CurrentPlanActivity>
    fun getCurrentPlanActivityById(id: Long): CurrentPlanActivity?
    fun updateCurrentPlanActivity(activityId: Long, activity: CurrentPlanActivity)
    fun createCurrentPlanActivity(activity: CurrentPlanActivity)
    fun deleteCurrentPlanActivityById(id: Long)
}
