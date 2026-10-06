package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.CurrentPlanActivities
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class CurrentPlanActivityDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CurrentPlanActivityDao>(CurrentPlanActivities)
    var date: OffsetDateTime by CurrentPlanActivities.date
    var userPlanId by CurrentPlanActivities.userPlan
    var activeWeekId by CurrentPlanActivities.activeWeek
    var activeDayId by CurrentPlanActivities.activeDay
}
