package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.Gyms
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class GymDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<GymDao>(Gyms)
    var name by Gyms.name
    var description by Gyms.description
    var schedule by Gyms.schedule
    var address by Gyms.address
    var phone by Gyms.phone
    var logoUrl by Gyms.logoUrl
    var bannerUrl by Gyms.bannerUrl
}
