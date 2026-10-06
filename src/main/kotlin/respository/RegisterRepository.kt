package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Register

interface RegisterRepository {
    fun getRegisters(): List<Register>
    fun getRegisterById(id: Long): Register?
    fun updateRegister(registerId: Long, register: Register)
    fun createRegister(register: Register)
    fun deleteRegisterById(id: Long)
}
