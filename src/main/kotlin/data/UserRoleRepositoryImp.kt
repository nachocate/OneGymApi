package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.UserRoleDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.UserRole
import com.concatstudio.onegym.respository.UserRoleRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRoleRepositoryImp : UserRoleRepository {
    override fun getUserRoles() = transaction(Database.connection) { UserRoleDao.all().map(::toModel) }
    override fun getUserRoleById(id: Long) = transaction(Database.connection) { UserRoleDao.findById(id)?.let(::toModel) }
    override fun updateUserRole(roleId: Long, role: UserRole) = transaction(Database.connection) { UserRoleDao.findById(roleId)?.apply { name = role.name }; Unit }
    override fun createUserRole(role: UserRole) = transaction(Database.connection) { UserRoleDao.new { name = role.name }; Unit }
    override fun deleteUserRoleById(id: Long) = transaction(Database.connection) { UserRoleDao.findById(id)?.delete(); Unit }
}
