package com.concatstudio.onegym.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

object Users : LongIdTable("users") {
    val firstName = varchar("firstname", 100)
    val lastName = varchar("lastname", 100)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
}

class UserDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserDao>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var email by Users.email
    var password by Users.password
}
