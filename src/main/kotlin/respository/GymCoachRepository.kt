package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.GymCoach

interface GymCoachRepository {
    fun getGymCoaches(): List<GymCoach>
    fun getGymCoachesByGymId(gymId: Long, limit: Int, offset: Long): List<GymCoach>
    fun getGymCoachById(id: Long): GymCoach?
    fun createGymCoach(gymCoach: GymCoach)
    fun updateGymCoach(id: Long, gymCoach: GymCoach)
    fun deleteGymCoachById(id: Long)
}
