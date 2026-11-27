package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.User
import com.concatstudio.onegym.model.UpdateUserProfileRequest

interface UserRepository {
    fun getUsers(): List<User>
    fun getUserById(id: Long): User?
    fun getUserByEmail(email: String): User?
    fun updateUser(userId:Long,user: User)
    fun updateUserProfile(userId: Long, profile: UpdateUserProfileRequest)
    fun createUser(user: User)
    fun deleteUserById(id: Long)
    fun checkPassword(userId: Long, password: String): Boolean
    fun changePassword(userId: Long, currentPassword: String, newPassword: String): Boolean
}
