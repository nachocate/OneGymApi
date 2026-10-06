package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.Users
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.CoachResponse
import com.concatstudio.onegym.model.CoachType
import com.concatstudio.onegym.model.UserResponse
import com.concatstudio.onegym.respository.GymHomeRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate

class GymHomeRepositoryImp : GymHomeRepository {
    override fun getActiveCoaches(gymId: Long): List<CoachResponse> = transaction(Database.connection) {
        val today = LocalDate.now()
        GymCoaches
            .innerJoin(Users)
            .innerJoin(CoachTypes)
            .selectAll()
            .where {
                (GymCoaches.gym eq gymId) and
                    (GymCoaches.startDate lessEq today) and
                    (GymCoaches.endDate.isNull() or (GymCoaches.endDate greaterEq today))
            }
            .orderBy(Users.lastName to SortOrder.ASC, Users.firstName to SortOrder.ASC)
            .map { row ->
                CoachResponse(
                    user = UserResponse(
                        id = row[Users.id].value,
                        firstName = row[Users.firstName],
                        lastName = row[Users.lastName],
                        email = row[Users.email],
                        avatarUrl = row[Users.avatarUrl],
                        phone = row[Users.phone],
                        address = row[Users.address]
                    ),
                    coachType = CoachType(
                        id = row[CoachTypes.id].value,
                        name = row[CoachTypes.name]
                    ),
                    startDate = row[GymCoaches.startDate].toString(),
                    endDate = row[GymCoaches.endDate]?.toString()
                )
            }
    }
}
