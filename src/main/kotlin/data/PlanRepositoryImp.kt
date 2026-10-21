package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.PlanDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Plan
import com.concatstudio.onegym.respository.PlanRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.*

class PlanRepositoryImp : PlanRepository {
    override fun getPlans() = transaction(Database.connection) { PlanDao.all().map(::toModel) }
    override fun getPlansByGymId(gymId: Long, limit: Int, offset: Long) = transaction(Database.connection) {
        PlanDao.find { Plans.gym eq gymId }.orderBy(Plans.id to SortOrder.ASC).drop(offset.toInt()).take(limit).map(::toModel)
    }
    override fun getPlanById(id: Long) = transaction(Database.connection) { PlanDao.findById(id)?.let(::toModel) }
    override fun updatePlan(planId: Long, plan: Plan) = transaction(Database.connection) { PlanDao.findById(planId)?.apply { name = plan.name; gymId = gymRef(plan.gymId); planTypeId = planTypeRef(plan.planTypeId); planRootId = plan.planRootId?.let(::planRef) }; Unit }
    override fun createPlan(plan: Plan) = transaction(Database.connection) { PlanDao.new { name = plan.name; gymId = gymRef(plan.gymId); planTypeId = planTypeRef(plan.planTypeId); planRootId = plan.planRootId?.let(::planRef) }; Unit }
    override fun deletePlanById(id: Long) = transaction(Database.connection) { PlanDao.findById(id)?.delete(); Unit }
}
