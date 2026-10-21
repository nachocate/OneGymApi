package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.GymCoach
import com.concatstudio.onegym.respository.GymCoachRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
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
    val repository: GymCoachRepository by inject(); val auth: AuthorizationService by inject()
    route("/gym-coaches") {
        get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repository.getGymCoaches()) }
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val item = repository.getGymCoachById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!auth.isSuperAdmin(actor) && auth.gymRole(actor, item.gymId) == null) call.respond(HttpStatusCode.Forbidden) else call.respond(item)
        }
        post {
            val item = call.receive<GymCoach>(); val actor = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!auth.canManageGym(actor, item.gymId)) call.respond(HttpStatusCode.Forbidden) else { repository.createGymCoach(item); call.respond(HttpStatusCode.Created) }
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val previous = repository.getGymCoachById(id) ?: return@put call.respond(HttpStatusCode.NotFound)
            val item = call.receive<GymCoach>(); val actor = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (item.gymId != previous.gymId || !auth.canManageGym(actor, previous.gymId)) call.respond(HttpStatusCode.Forbidden) else { repository.updateGymCoach(id, item); call.respond(HttpStatusCode.NoContent) }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val item = repository.getGymCoachById(id) ?: return@delete call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (!auth.canManageGym(actor, item.gymId)) call.respond(HttpStatusCode.Forbidden) else { repository.deleteGymCoachById(id); call.respond(HttpStatusCode.NoContent) }
        }
    }
}
