package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.UserGym
import com.concatstudio.onegym.respository.UserGymRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userGymRouting() {
    val repository: UserGymRepository by inject()
    route("/user-gyms") {
        get { call.respond(repository.getUserGyms()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getUserGymById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createUserGym(call.receive<UserGym>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getUserGymById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateUserGym(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteUserGymById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
