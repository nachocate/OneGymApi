package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.QuantityType

interface QuantityTypeRepository {
    fun getQuantityTypes(): List<QuantityType>
    fun getQuantityTypeById(id: Long): QuantityType?
    fun updateQuantityType(quantityTypeId: Long, quantityType: QuantityType)
    fun createQuantityType(quantityType: QuantityType)
    fun deleteQuantityTypeById(id: Long)
}
