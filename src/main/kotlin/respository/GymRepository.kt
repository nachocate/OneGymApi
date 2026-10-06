package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Gym

interface GymRepository {
    fun getGyms(): List<Gym>
    fun getGymById(id: Long): Gym?
    fun updateGym(gymId: Long, gym: Gym)
    fun createGym(gym: Gym)
    fun deleteGymById(id: Long)
}
