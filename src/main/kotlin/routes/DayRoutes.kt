package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Day
import com.concatstudio.onegym.respository.DayRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.dayRouting() {
    val repository: DayRepository by inject()
    route("/days") {
        get { call.respond(repository.getDays()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getDayById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createDay(call.receive<Day>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getDayById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateDay(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteDayById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
