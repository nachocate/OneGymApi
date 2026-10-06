package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.UserRoles
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class UserRoleDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserRoleDao>(UserRoles)
    var name by UserRoles.name
}
