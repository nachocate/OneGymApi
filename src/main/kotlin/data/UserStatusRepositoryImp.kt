package com.concatstudio.onegym.data

import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.Gym
import com.concatstudio.onegym.model.GymSubscription
import com.concatstudio.onegym.model.UserRole
import com.concatstudio.onegym.respository.UserStatusRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate

class UserStatusRepositoryImp : UserStatusRepository {
    override fun getActiveGymSubscriptions(userId: Long): List<GymSubscription> = transaction(Database.connection) {
        val today = LocalDate.now()
        UserGyms
            .innerJoin(Gyms)
            .innerJoin(UserRoles)
            .selectAll()
            .where {
                (UserGyms.user eq userId) and
                    (UserGyms.startDate lessEq today) and
                    (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
            }
            .map { row ->
                GymSubscription(
                    gym = Gym(
                        id = row[Gyms.id].value,
                        name = row[Gyms.name],
                        description = row[Gyms.description],
                        schedule = row[Gyms.schedule],
                        address = row[Gyms.address],
                        phone = row[Gyms.phone],
                        logoUrl = row[Gyms.logoUrl],
                        bannerUrl = row[Gyms.bannerUrl]
                    ),
                    role = UserRole(
                        id = row[UserRoles.id].value,
                        name = row[UserRoles.name]
                    )
                )
            }
    }

    override fun hasActiveGymSubscription(userId: Long, gymId: Long): Boolean = transaction(Database.connection) {
        val today = LocalDate.now()
        UserGyms.selectAll()
            .where {
                (UserGyms.user eq userId) and
                    (UserGyms.gym eq gymId) and
                    (UserGyms.startDate lessEq today) and
                    (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
            }
            .count() > 0
    }
}
