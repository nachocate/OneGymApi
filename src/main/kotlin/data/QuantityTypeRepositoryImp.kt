package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.QuantityTypeDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.QuantityType
import com.concatstudio.onegym.respository.QuantityTypeRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class QuantityTypeRepositoryImp : QuantityTypeRepository {
    override fun getQuantityTypes() = transaction(Database.connection) { QuantityTypeDao.all().map(::toModel) }
    override fun getQuantityTypeById(id: Long) = transaction(Database.connection) { QuantityTypeDao.findById(id)?.let(::toModel) }
    override fun updateQuantityType(quantityTypeId: Long, quantityType: QuantityType) = transaction(Database.connection) { QuantityTypeDao.findById(quantityTypeId)?.apply { name = quantityType.name }; Unit }
    override fun createQuantityType(quantityType: QuantityType) = transaction(Database.connection) { QuantityTypeDao.new { name = quantityType.name }; Unit }
    override fun deleteQuantityTypeById(id: Long) = transaction(Database.connection) { QuantityTypeDao.findById(id)?.delete(); Unit }
}
