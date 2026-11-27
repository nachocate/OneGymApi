package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.GymSubscription

interface UserStatusRepository {
    fun getActiveGymSubscriptions(userId: Long): List<GymSubscription>
    fun getSystemRoleNames(userId: Long): List<String>
    fun hasActiveGymSubscription(userId: Long, gymId: Long): Boolean
}
