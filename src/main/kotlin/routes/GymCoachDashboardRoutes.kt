package com.concatstudio.onegym.routes

import com.concatstudio.onegym.data.DomainConflictException
import com.concatstudio.onegym.data.DomainNotFoundException
import com.concatstudio.onegym.data.GymCoachManagementService
import com.concatstudio.onegym.model.AssignExistingGymCoachRequest
import com.concatstudio.onegym.model.RegisterGymCoachRequest
import com.concatstudio.onegym.model.UpdateGymCoachRequest
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.GymRole
import com.concatstudio.onegym.security.authenticatedUserId
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

/** Friendly gym-scoped write API for dashboard coach administration. */
fun Route.gymCoachDashboardRouting() {
    val service: GymCoachManagementService by inject()
    val authorization: AuthorizationService by inject()

    route("/gyms/{gymId}/coaches") {
        get("/details") {
            val gymId = call.gymIdOrBadRequest() ?: return@get
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.isSuperAdmin(actorId) && authorization.gymRole(actorId, gymId) == null) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }
            val page = call.pageRequest() ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(service.list(gymId, page.limit, page.offset))
        }
        post {
            val gymId = call.gymIdOrBadRequest() ?: return@post
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canAssignGymRole(actorId, gymId, GymRole.COACH)) return@post call.respond(HttpStatusCode.Forbidden)
            call.respondCoachOperation { service.register(gymId, call.receive<RegisterGymCoachRequest>()) }
        }
        post("/assignments") {
            val gymId = call.gymIdOrBadRequest() ?: return@post
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<AssignExistingGymCoachRequest>()
            if (!authorization.canManageCoach(actorId, gymId, request.userId)) return@post call.respond(HttpStatusCode.Forbidden)
            call.respondCoachOperation { service.assignExisting(gymId, request) }
        }
        put("/{coachId}") {
            val gymId = call.gymIdOrBadRequest() ?: return@put
            val coachId = call.parameters["coachId"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val targetUserId = try { service.userIdForAssignment(gymId, coachId) }
            catch (error: DomainNotFoundException) { return@put call.respond(HttpStatusCode.NotFound) }
            if (!authorization.canManageCoach(actorId, gymId, targetUserId)) return@put call.respond(HttpStatusCode.Forbidden)
            call.respondCoachOperation(status = HttpStatusCode.OK) { service.update(gymId, coachId, call.receive<UpdateGymCoachRequest>()) }
        }
        delete("/{coachId}") {
            val gymId = call.gymIdOrBadRequest() ?: return@delete
            val coachId = call.parameters["coachId"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val targetUserId = try { service.userIdForAssignment(gymId, coachId) }
            catch (error: DomainNotFoundException) { return@delete call.respond(HttpStatusCode.NotFound) }
            if (!authorization.canManageCoach(actorId, gymId, targetUserId)) return@delete call.respond(HttpStatusCode.Forbidden)
            try {
                service.delete(gymId, coachId)
                call.respond(HttpStatusCode.NoContent)
            } catch (error: DomainNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private fun AuthorizationService.canManageCoach(actorId: Long, gymId: Long, targetUserId: Long): Boolean {
    val targetRole = gymRole(targetUserId, gymId) ?: GymRole.COACH
    return canAssignGymRole(actorId, gymId, targetRole)
}

private suspend fun io.ktor.server.application.ApplicationCall.respondCoachOperation(
    status: HttpStatusCode = HttpStatusCode.Created,
    operation: suspend () -> Any
) {
    try {
        respond(status, operation())
    } catch (error: DomainNotFoundException) {
        respond(HttpStatusCode.NotFound)
    } catch (error: DomainConflictException) {
        respond(HttpStatusCode.Conflict)
    } catch (error: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest)
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.gymIdOrBadRequest(): Long? =
    parameters["gymId"]?.toLongOrNull() ?: run {
        respond(HttpStatusCode.BadRequest)
        null
    }
