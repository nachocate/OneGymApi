package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Gym
import com.concatstudio.onegym.respository.GymRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.gymRouting() {
    val repository: GymRepository by inject()
    route("/gyms") {
        get { call.respond(repository.getGyms()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getGymById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createGym(call.receive<Gym>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getGymById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateGym(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteGymById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
