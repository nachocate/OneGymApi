package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.NewsDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.News
import com.concatstudio.onegym.respository.NewsRepository
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class NewsRepositoryImp : NewsRepository {
    override fun getNews() = transaction(Database.connection) { NewsDao.all().map(::toModel) }
    override fun getNewsById(id: Long) = transaction(Database.connection) { NewsDao.findById(id)?.let(::toModel) }
    override fun updateNews(newsId: Long, news: News) = transaction(Database.connection) { NewsDao.findById(newsId)?.apply { gymId = gymRef(news.gymId); title = news.title; description = news.description; date = OffsetDateTime.parse(news.date) }; Unit }
    override fun createNews(news: News) = transaction(Database.connection) { NewsDao.new { gymId = gymRef(news.gymId); title = news.title; description = news.description; date = OffsetDateTime.parse(news.date) }; Unit }
    override fun deleteNewsById(id: Long) = transaction(Database.connection) { NewsDao.findById(id)?.delete(); Unit }
}
