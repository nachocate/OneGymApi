package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.UserDao
import com.concatstudio.onegym.dao.Users
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.User
import com.concatstudio.onegym.respository.UserRepository
import com.concatstudio.onegym.security.PasswordService
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.*
import com.concatstudio.onegym.mappers.toModel

class UserRepositoryImp : UserRepository {
    override fun getUsers(): List<User> {
        return transaction(Database.connection) {
            UserDao.all().map(::toModel)
        }
    }

    override fun getUserById(id: Long): User? {
        return transaction(Database.connection) {
            UserDao.findById(id)?.let(::toModel)
        }
    }

    override fun getUserByEmail(email: String): User? {
        return transaction(Database.connection) {
            UserDao.find { Users.email eq email }.firstOrNull()?.let(::toModel)
        }
    }

    override fun updateUser(userId: Long, user: User) {
        transaction(Database.connection) {
            UserDao.findById(userId)?.apply {
                firstName = user.firstName
                lastName = user.lastName
                email = user.email
                password = PasswordService.encode(user.password)
            }
        }
    }

    override fun deleteUserById(id: Long) {
        transaction(Database.connection) {
            UserDao.findById(id)?.delete()
        }
    }

    override fun createUser(user: User) {
        transaction(Database.connection) {
            UserDao.new {
                firstName = user.firstName
                lastName = user.lastName
                email = user.email
                password = PasswordService.encode(user.password)
            }
        }
    }

    override fun changePassword(userId: Long, currentPassword: String, newPassword: String): Boolean {
        return transaction(Database.connection) {
            val user = UserDao.findById(userId) ?: return@transaction false
            if (!PasswordService.matches(currentPassword, user.password)) return@transaction false
            user.password = PasswordService.encode(newPassword)
            true
        }
    }

    override fun checkPassword(userId: Long, password: String): Boolean {
        return transaction(Database.connection) {
            UserDao.findById(userId)?.let { PasswordService.matches(password, it.password) } ?: false
        }
    }


}
