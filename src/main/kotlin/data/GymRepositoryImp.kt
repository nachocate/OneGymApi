package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.GymDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.Gym
import com.concatstudio.onegym.respository.GymRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class GymRepositoryImp : GymRepository {
    override fun getGyms() = transaction(Database.connection) { GymDao.all().map(::toModel) }
    override fun getGymById(id: Long) = transaction(Database.connection) { GymDao.findById(id)?.let(::toModel) }
    override fun updateGym(gymId: Long, gym: Gym) = transaction(Database.connection) { GymDao.findById(gymId)?.apply { name = gym.name; description = gym.description; schedule = gym.schedule; address = gym.address; phone = gym.phone; logoUrl = gym.logoUrl; bannerUrl = gym.bannerUrl }; Unit }
    override fun createGym(gym: Gym) = transaction(Database.connection) { GymDao.new { name = gym.name; description = gym.description; schedule = gym.schedule; address = gym.address; phone = gym.phone; logoUrl = gym.logoUrl; bannerUrl = gym.bannerUrl }; Unit }
    override fun deleteGymById(id: Long) = transaction(Database.connection) { GymDao.findById(id)?.delete(); Unit }
}
