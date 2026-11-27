package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.CurrentPlanActivityDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.CurrentPlanActivity
import com.concatstudio.onegym.respository.CurrentPlanActivityRepository
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class CurrentPlanActivityRepositoryImp : CurrentPlanActivityRepository {
    override fun getCurrentPlanActivities() = transaction(Database.connection) { CurrentPlanActivityDao.all().map(::toModel) }
    override fun getCurrentPlanActivityById(id: Long) = transaction(Database.connection) { CurrentPlanActivityDao.findById(id)?.let(::toModel) }
    override fun updateCurrentPlanActivity(activityId: Long, activity: CurrentPlanActivity) = transaction(Database.connection) { CurrentPlanActivityDao.findById(activityId)?.apply { date = OffsetDateTime.parse(activity.date); userPlanId = userPlanRef(activity.userPlanId); activeWeekId = activity.activeWeekId?.let(::weekRef); activeDayId = activity.activeDayId?.let(::dayRef); isActive = activity.isActive }; Unit }
    override fun createCurrentPlanActivity(activity: CurrentPlanActivity) = transaction(Database.connection) { CurrentPlanActivityDao.new { date = OffsetDateTime.parse(activity.date); userPlanId = userPlanRef(activity.userPlanId); activeWeekId = activity.activeWeekId?.let(::weekRef); activeDayId = activity.activeDayId?.let(::dayRef); isActive = activity.isActive }; Unit }
    override fun deleteCurrentPlanActivityById(id: Long) = transaction(Database.connection) { CurrentPlanActivityDao.findById(id)?.delete(); Unit }
}
