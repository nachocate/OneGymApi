package com.concatstudio.onegym.dao

import com.concatstudio.onegym.data.News
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class NewsDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<NewsDao>(News)
    var gymId by News.gym
    var title by News.title
    var description by News.description
    var date: OffsetDateTime by News.date
}
