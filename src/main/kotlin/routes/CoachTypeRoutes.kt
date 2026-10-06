package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.CoachType
import com.concatstudio.onegym.respository.CoachTypeRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.coachTypeRouting() {
    val repository: CoachTypeRepository by inject()
    route("/coach-types") {
        get { call.respond(repository.getCoachTypes()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getCoachTypeById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createCoachType(call.receive<CoachType>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getCoachTypeById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateCoachType(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteCoachTypeById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
