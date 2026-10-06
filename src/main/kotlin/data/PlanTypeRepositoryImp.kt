package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.PlanTypeDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.PlanType
import com.concatstudio.onegym.respository.PlanTypeRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class PlanTypeRepositoryImp : PlanTypeRepository {
    override fun getPlanTypes() = transaction(Database.connection) { PlanTypeDao.all().map(::toModel) }
    override fun getPlanTypeById(id: Long) = transaction(Database.connection) { PlanTypeDao.findById(id)?.let(::toModel) }
    override fun updatePlanType(planTypeId: Long, planType: PlanType) = transaction(Database.connection) { PlanTypeDao.findById(planTypeId)?.apply { name = planType.name }; Unit }
    override fun createPlanType(planType: PlanType) = transaction(Database.connection) { PlanTypeDao.new { name = planType.name }; Unit }
    override fun deletePlanTypeById(id: Long) = transaction(Database.connection) { PlanTypeDao.findById(id)?.delete(); Unit }
}
