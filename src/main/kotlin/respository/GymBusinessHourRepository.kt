package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.GymBusinessHour

interface GymBusinessHourRepository {
    fun getGymBusinessHours(): List<GymBusinessHour>
    fun getGymBusinessHoursByGymId(gymId: Long, limit: Int, offset: Long): List<GymBusinessHour>
    fun getGymBusinessHourById(id: Long): GymBusinessHour?
    fun createGymBusinessHour(gymBusinessHour: GymBusinessHour)
    fun updateGymBusinessHour(id: Long, gymBusinessHour: GymBusinessHour)
    fun deleteGymBusinessHourById(id: Long)
}
