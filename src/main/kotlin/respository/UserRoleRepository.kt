package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.UserRole

interface UserRoleRepository {
    fun getUserRoles(): List<UserRole>
    fun getUserRoleById(id: Long): UserRole?
    fun updateUserRole(roleId: Long, role: UserRole)
    fun createUserRole(role: UserRole)
    fun deleteUserRoleById(id: Long)
}
