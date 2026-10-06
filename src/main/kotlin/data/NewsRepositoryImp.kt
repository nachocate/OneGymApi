package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.NewsDao
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.mappers.toModel
import com.concatstudio.onegym.model.News as NewsModel
import com.concatstudio.onegym.respository.NewsRepository
import java.time.OffsetDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class NewsRepositoryImp : NewsRepository {
    override fun getNews() = transaction(Database.connection) { NewsDao.all().map(::toModel) }
    override fun getNewsByGymId(gymId: Long): List<NewsModel> = transaction(Database.connection) {
        News.selectAll()
            .where { News.gym eq gymId }
            .orderBy(News.date to SortOrder.DESC)
            .map { row ->
                NewsModel(
                    id = row[News.id].value,
                    gymId = row[News.gym].value,
                    title = row[News.title],
                    description = row[News.description],
                    date = row[News.date].toString(),
                    imageUrl = row[News.imageUrl]
                )
            }
    }
    override fun getNewsById(id: Long) = transaction(Database.connection) { NewsDao.findById(id)?.let(::toModel) }
    override fun updateNews(newsId: Long, news: NewsModel) = transaction(Database.connection) { NewsDao.findById(newsId)?.apply { gymId = gymRef(news.gymId); title = news.title; description = news.description; imageUrl = news.imageUrl; date = OffsetDateTime.parse(news.date) }; Unit }
    override fun createNews(news: NewsModel) = transaction(Database.connection) { NewsDao.new { gymId = gymRef(news.gymId); title = news.title; description = news.description; imageUrl = news.imageUrl; date = OffsetDateTime.parse(news.date) }; Unit }
    override fun deleteNewsById(id: Long) = transaction(Database.connection) { NewsDao.findById(id)?.delete(); Unit }
}
