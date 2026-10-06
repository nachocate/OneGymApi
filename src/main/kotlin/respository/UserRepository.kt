package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.User

interface UserRepository {
    fun getUsers(): List<User>
    fun getUserById(id: Long): User?
    fun updateUser(userId:Long,user: User)
    fun createUser(user: User)
    fun deleteUserById(id: Long)
    fun checkPassword(userId: Long, password: String): Boolean
    fun changePassword(userId: Long, currentPassword: String, newPassword: String): Boolean
}
