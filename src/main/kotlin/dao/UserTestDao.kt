package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.UserTests
import java.math.BigDecimal
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class UserTestDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserTestDao>(UserTests)
    var exerciseId by UserTests.exercise
    var userId by UserTests.user
    var quantityTypeId by UserTests.quantityType
    var repetitions by UserTests.repetitions
    var quantity: BigDecimal? by UserTests.quantity
    var date: OffsetDateTime by UserTests.date
    var description by UserTests.description
}
