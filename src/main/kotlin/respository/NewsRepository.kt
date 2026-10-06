package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.News

interface NewsRepository {
    fun getNews(): List<News>
    fun getNewsById(id: Long): News?
    fun updateNews(newsId: Long, news: News)
    fun createNews(news: News)
    fun deleteNewsById(id: Long)
}
