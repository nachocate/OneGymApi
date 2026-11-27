package com.concatstudio.onegym.routes

import com.concatstudio.onegym.data.DomainConflictException
import com.concatstudio.onegym.data.DomainNotFoundException
import com.concatstudio.onegym.data.GymManagementService
import com.concatstudio.onegym.model.CreateGymRequest
import com.concatstudio.onegym.model.Gym
import com.concatstudio.onegym.respository.GymRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.gymRouting() {
    val repository: GymRepository by inject()
    val managementService: GymManagementService by inject()
    val authorizationService: AuthorizationService by inject()
    route("/gyms") {
        get {
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.isSuperAdmin(actorId)) return@get call.respond(HttpStatusCode.Forbidden)
            call.respond(repository.getGyms())
        }
        get("/{id}") {
            val gymId = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.isSuperAdmin(actorId) && authorizationService.gymRole(actorId, gymId) == null) return@get call.respond(HttpStatusCode.Forbidden)
            repository.getGymById(gymId)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        post {
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.isSuperAdmin(actorId)) return@post call.respond(HttpStatusCode.Forbidden)
            try {
                call.respond(HttpStatusCode.Created, managementService.createGym(call.receive<CreateGymRequest>()))
            } catch (error: DomainNotFoundException) { call.respond(HttpStatusCode.NotFound) }
            catch (error: DomainConflictException) { call.respond(HttpStatusCode.Conflict) }
            catch (error: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest) }
        }
        put("/{id}") {
            val gymId = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.canManageGym(actorId, gymId)) return@put call.respond(HttpStatusCode.Forbidden)
            if (repository.getGymById(gymId) == null) return@put call.respond(HttpStatusCode.NotFound)
            repository.updateGym(gymId, call.receive<Gym>())
            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val gymId = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.isSuperAdmin(actorId)) return@delete call.respond(HttpStatusCode.Forbidden)
            repository.deleteGymById(gymId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
