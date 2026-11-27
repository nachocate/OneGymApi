package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.GymBusinessHour
import com.concatstudio.onegym.respository.GymBusinessHourRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/** Informational opening hours; they never gate business operations. */
fun Route.gymBusinessHourRouting() {
    val repository: GymBusinessHourRepository by inject()
    val authorizationService: AuthorizationService by inject()
    route("/gym-business-hours") {
        get {
            if (!call.isSuperAdmin(authorizationService)) return@get call.respond(HttpStatusCode.Forbidden)
            call.respond(repository.getGymBusinessHours())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val hour = repository.getGymBusinessHourById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.isSuperAdmin(actorId) && authorizationService.gymRole(actorId, hour.gymId) == null) return@get call.respond(HttpStatusCode.Forbidden)
            call.respond(hour)
        }
        post {
            val hour = call.receive<GymBusinessHour>()
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.canManageGym(actorId, hour.gymId)) return@post call.respond(HttpStatusCode.Forbidden)
            repository.createGymBusinessHour(hour)
            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val storedHour = repository.getGymBusinessHourById(id) ?: return@put call.respond(HttpStatusCode.NotFound)
            val replacement = call.receive<GymBusinessHour>()
            val actorId = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.canManageGym(actorId, storedHour.gymId) || storedHour.gymId != replacement.gymId) return@put call.respond(HttpStatusCode.Forbidden)
            repository.updateGymBusinessHour(id, replacement)
            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val hour = repository.getGymBusinessHourById(id) ?: return@delete call.respond(HttpStatusCode.NotFound)
            val actorId = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.canManageGym(actorId, hour.gymId)) return@delete call.respond(HttpStatusCode.Forbidden)
            repository.deleteGymBusinessHourById(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
