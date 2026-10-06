package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.News
import com.concatstudio.onegym.respository.NewsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.newsRouting() {
    val repository: NewsRepository by inject()
    route("/news") {
        get { call.respond(repository.getNews()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getNewsById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createNews(call.receive<News>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getNewsById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateNews(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteNewsById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
