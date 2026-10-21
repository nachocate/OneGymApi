package com.concatstudio.onegym.security

import com.concatstudio.onegym.data.SystemRoles
import com.concatstudio.onegym.data.UserGyms
import com.concatstudio.onegym.data.UserRoles
import com.concatstudio.onegym.data.UserSystemRoles
import com.concatstudio.onegym.dao.BlockDao
import com.concatstudio.onegym.dao.BlockExerciseDao
import com.concatstudio.onegym.dao.DayDao
import com.concatstudio.onegym.dao.PlanDao
import com.concatstudio.onegym.dao.UserPlanDao
import com.concatstudio.onegym.dao.CurrentPlanActivityDao
import com.concatstudio.onegym.dao.RegisterDao
import com.concatstudio.onegym.dao.WeekDao
import com.concatstudio.onegym.database.Database
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate

enum class GymRole(val rank: Int) {
    MEMBER(1), COACH(2), ADMIN(3), OWNER(4);

    companion object {
        fun fromName(name: String): GymRole? = entries.firstOrNull { it.name == name.trim().uppercase() }
    }
}

class AuthorizationService {
    fun isSuperAdmin(userId: Long): Boolean = transaction(Database.connection) {
        UserSystemRoles
            .innerJoin(SystemRoles)
            .selectAll()
            .where { (UserSystemRoles.user eq userId) and (SystemRoles.name eq "SUPER_ADMIN") }
            .any()
    }

    fun gymRole(userId: Long, gymId: Long): GymRole? = transaction(Database.connection) {
        val today = LocalDate.now()
        UserGyms
            .innerJoin(UserRoles)
            .selectAll()
            .where {
                (UserGyms.user eq userId) and
                    (UserGyms.gym eq gymId) and
                    (UserGyms.startDate lessEq today) and
                    (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
            }
            .mapNotNull { GymRole.fromName(it[UserRoles.name]) }
            .maxByOrNull { it.rank }
    }

    fun canManageGym(userId: Long, gymId: Long): Boolean =
        isSuperAdmin(userId) || (gymRole(userId, gymId)?.rank ?: 0) >= GymRole.ADMIN.rank

    fun canManageTraining(userId: Long, gymId: Long): Boolean =
        isSuperAdmin(userId) || (gymRole(userId, gymId)?.rank ?: 0) >= GymRole.COACH.rank

    fun hasActiveGymMembership(userId: Long, gymId: Long): Boolean = transaction(Database.connection) {
        val today = LocalDate.now()
        UserGyms.selectAll().where {
            (UserGyms.user eq userId) and (UserGyms.gym eq gymId) and
                (UserGyms.startDate lessEq today) and
                (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
        }.any()
    }

    fun gymForPlan(planId: Long): Long? = transaction(Database.connection) { PlanDao.findById(planId)?.gymId?.value }
    fun gymForWeek(weekId: Long): Long? = transaction(Database.connection) { WeekDao.findById(weekId)?.planId?.value?.let { PlanDao.findById(it)?.gymId?.value } }
    fun gymForDay(dayId: Long): Long? = transaction(Database.connection) { DayDao.findById(dayId)?.weekId?.value?.let { WeekDao.findById(it)?.planId?.value }?.let { PlanDao.findById(it)?.gymId?.value } }
    fun gymForBlock(blockId: Long): Long? = transaction(Database.connection) { BlockDao.findById(blockId)?.dayId?.value?.let { DayDao.findById(it)?.weekId?.value }?.let { WeekDao.findById(it)?.planId?.value }?.let { PlanDao.findById(it)?.gymId?.value } }
    fun gymForBlockExercise(blockExerciseId: Long): Long? = transaction(Database.connection) { BlockExerciseDao.findById(blockExerciseId)?.blockId?.value?.let(::gymForBlock) }
    fun gymForUserPlan(userPlanId: Long): Long? = transaction(Database.connection) { UserPlanDao.findById(userPlanId)?.planId?.let { PlanDao.findById(it)?.gymId?.value } }
    fun userForUserPlan(userPlanId: Long): Long? = transaction(Database.connection) { UserPlanDao.findById(userPlanId)?.userId?.value }
    fun gymForCurrentPlanActivity(activityId: Long): Long? = transaction(Database.connection) { CurrentPlanActivityDao.findById(activityId)?.userPlanId?.value?.let(::gymForUserPlan) }
    fun userForCurrentPlanActivity(activityId: Long): Long? = transaction(Database.connection) { CurrentPlanActivityDao.findById(activityId)?.userPlanId?.value?.let(::userForUserPlan) }
    fun gymForRegister(registerId: Long): Long? = transaction(Database.connection) { RegisterDao.findById(registerId)?.userPlanId?.value?.let(::gymForUserPlan) }
    fun userForRegister(registerId: Long): Long? = transaction(Database.connection) { RegisterDao.findById(registerId)?.userPlanId?.value?.let(::userForUserPlan) }

    fun canAssignGymRole(userId: Long, gymId: Long, targetRole: GymRole): Boolean {
        if (isSuperAdmin(userId)) return true
        val actorRole = gymRole(userId, gymId) ?: return false
        return actorRole == GymRole.OWNER || actorRole.rank > targetRole.rank
    }

    fun canAccessUserThroughGym(actorUserId: Long, targetUserId: Long): Boolean {
        if (actorUserId == targetUserId || isSuperAdmin(actorUserId)) return true
        return transaction(Database.connection) {
            val today = LocalDate.now()
            fun activeGymIds(userId: Long) = UserGyms.selectAll().where {
                (UserGyms.user eq userId) and
                    (UserGyms.startDate lessEq today) and
                    (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
            }.map { it[UserGyms.gym].value }.toSet()
            activeGymIds(actorUserId).intersect(activeGymIds(targetUserId)).isNotEmpty()
        }
    }

    fun canManageUserTrainingData(actorUserId: Long, targetUserId: Long): Boolean {
        if (isSuperAdmin(actorUserId)) return true
        return transaction(Database.connection) {
            val today = LocalDate.now()
            val actorGyms = UserGyms.innerJoin(UserRoles).selectAll().where {
                (UserGyms.user eq actorUserId) and (UserGyms.startDate lessEq today) and
                    (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
            }.filter { (GymRole.fromName(it[UserRoles.name])?.rank ?: 0) >= GymRole.COACH.rank }
                .map { it[UserGyms.gym].value }.toSet()
            val targetGyms = UserGyms.selectAll().where {
                (UserGyms.user eq targetUserId) and (UserGyms.startDate lessEq today) and
                    (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
            }.map { it[UserGyms.gym].value }.toSet()
            actorGyms.intersect(targetGyms).isNotEmpty()
        }
    }
}
