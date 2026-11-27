package com.concatstudio.onegym.routes

import com.concatstudio.onegym.respository.UserGymRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/** Legacy administrative endpoint. Gym-scoped member operations live under /gyms/{gymId}/members. */
fun Route.userGymRouting() {
    val repository: UserGymRepository by inject()
    val authorizationService: AuthorizationService by inject()
    route("/user-gyms") {
        get { if (!call.isSuperAdmin(authorizationService)) call.respond(HttpStatusCode.Forbidden) else call.respond(repository.getUserGyms()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else if (!call.isSuperAdmin(authorizationService)) call.respond(HttpStatusCode.Forbidden) else repository.getUserGymById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { if (!call.isSuperAdmin(authorizationService)) call.respond(HttpStatusCode.Forbidden) else { repository.createUserGym(call.receive()); call.respond(HttpStatusCode.Created) } }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else if (!call.isSuperAdmin(authorizationService)) call.respond(HttpStatusCode.Forbidden) else if (repository.getUserGymById(id) == null) call.respond(HttpStatusCode.NotFound) else { repository.updateUserGym(id, call.receive()); call.respond(HttpStatusCode.NoContent) } }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else if (!call.isSuperAdmin(authorizationService)) call.respond(HttpStatusCode.Forbidden) else { repository.deleteUserGymById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
