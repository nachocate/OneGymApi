package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.RegisterDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Register
import com.concatstudio.onegym.respository.RegisterRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class RegisterRepositoryImp : RegisterRepository {
    override fun getRegisters() = transaction(Database.connection) { RegisterDao.all().map(::toModel) }
    override fun getRegisterById(id: Long) = transaction(Database.connection) { RegisterDao.findById(id)?.let(::toModel) }
    override fun updateRegister(registerId: Long, register: Register) = transaction(Database.connection) { RegisterDao.findById(registerId)?.apply { userPlanId = userPlanRef(register.userPlanId); exerciseId = exerciseRef(register.exerciseId); blockId = blockRef(register.blockId); weight = register.weight.toBigDecimal() }; Unit }
    override fun createRegister(register: Register) = transaction(Database.connection) { RegisterDao.new { userPlanId = userPlanRef(register.userPlanId); exerciseId = exerciseRef(register.exerciseId); blockId = blockRef(register.blockId); weight = register.weight.toBigDecimal() }; Unit }
    override fun deleteRegisterById(id: Long) = transaction(Database.connection) { RegisterDao.findById(id)?.delete(); Unit }
}
