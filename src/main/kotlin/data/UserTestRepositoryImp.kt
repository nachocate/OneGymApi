package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.UserTestDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.UserTest
import com.concatstudio.onegym.respository.UserTestRepository
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserTestRepositoryImp : UserTestRepository {
    override fun getUserTests() = transaction(Database.connection) { UserTestDao.all().map(::toModel) }
    override fun getUserTestById(id: Long) = transaction(Database.connection) { UserTestDao.findById(id)?.let(::toModel) }
    override fun updateUserTest(userTestId: Long, userTest: UserTest) = transaction(Database.connection) { UserTestDao.findById(userTestId)?.apply { exerciseId = exerciseRef(userTest.exerciseId); userId = userRef(userTest.userId); evaluationId = evaluationRef(userTest.evaluationId); quantityTypeId = userTest.quantityTypeId?.let(::quantityTypeRef); repetitions = userTest.repetitions; quantity = userTest.quantity?.toBigDecimal(); date = OffsetDateTime.parse(userTest.date); description = userTest.description }; Unit }
    override fun createUserTest(userTest: UserTest) = transaction(Database.connection) { UserTestDao.new { exerciseId = exerciseRef(userTest.exerciseId); userId = userRef(userTest.userId); evaluationId = evaluationRef(userTest.evaluationId); quantityTypeId = userTest.quantityTypeId?.let(::quantityTypeRef); repetitions = userTest.repetitions; quantity = userTest.quantity?.toBigDecimal(); date = OffsetDateTime.parse(userTest.date); description = userTest.description }; Unit }
    override fun deleteUserTestById(id: Long) = transaction(Database.connection) { UserTestDao.findById(id)?.delete(); Unit }
}
