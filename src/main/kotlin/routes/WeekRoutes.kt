package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Week
import com.concatstudio.onegym.respository.WeekRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.weekRouting() {
    val repository: WeekRepository by inject()
    route("/weeks") {
        get { call.respond(repository.getWeeks()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getWeekById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createWeek(call.receive<Week>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getWeekById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateWeek(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteWeekById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
