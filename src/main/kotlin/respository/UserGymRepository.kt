package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.UserGym

interface UserGymRepository {
    fun getUserGyms(): List<UserGym>
    fun getUserGymById(id: Long): UserGym?
    fun updateUserGym(userGymId: Long, userGym: UserGym)
    fun createUserGym(userGym: UserGym)
    fun deleteUserGymById(id: Long)
}
