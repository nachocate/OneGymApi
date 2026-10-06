package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.GymCoach
import com.concatstudio.onegym.respository.GymCoachRepository
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

fun Route.gymCoachRouting() {
    val repository: GymCoachRepository by inject()
    route("/gym-coaches") {
        get { call.respond(repository.getGymCoaches()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getGymCoachById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createGymCoach(call.receive<GymCoach>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getGymCoachById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateGymCoach(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteGymCoachById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
