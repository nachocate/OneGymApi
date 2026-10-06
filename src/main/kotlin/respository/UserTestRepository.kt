package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.UserTest

interface UserTestRepository {
    fun getUserTests(): List<UserTest>
    fun getUserTestById(id: Long): UserTest?
    fun updateUserTest(userTestId: Long, userTest: UserTest)
    fun createUserTest(userTest: UserTest)
    fun deleteUserTestById(id: Long)
}
