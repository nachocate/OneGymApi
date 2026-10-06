package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.UserPlanDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.UserPlan
import com.concatstudio.onegym.respository.UserPlanRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserPlanRepositoryImp : UserPlanRepository {
    override fun getUserPlans() = transaction(Database.connection) { UserPlanDao.all().map(::toModel) }
    override fun getUserPlanById(id: Long) = transaction(Database.connection) { UserPlanDao.findById(id)?.let(::toModel) }
    override fun updateUserPlan(userPlanId: Long, userPlan: UserPlan) = transaction(Database.connection) { UserPlanDao.findById(userPlanId)?.apply { userId = userRef(userPlan.userId); planId = planRef(userPlan.planId) }; Unit }
    override fun createUserPlan(userPlan: UserPlan) = transaction(Database.connection) { UserPlanDao.new { userId = userRef(userPlan.userId); planId = planRef(userPlan.planId) }; Unit }
    override fun deleteUserPlanById(id: Long) = transaction(Database.connection) { UserPlanDao.findById(id)?.delete(); Unit }
}
