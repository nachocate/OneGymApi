package com.concatstudio.onegym.routes

import com.concatstudio.onegym.data.DomainConflictException
import com.concatstudio.onegym.data.DomainNotFoundException
import com.concatstudio.onegym.model.AssignPlanificationRequest
import com.concatstudio.onegym.model.ClonePlanificationRequest
import com.concatstudio.onegym.model.SavePlanificationRequest
import com.concatstudio.onegym.respository.PlanificationRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

/** High-level, atomic planning routes for the dashboard planification editor. */
fun Route.planificationRouting() {
    val repository: PlanificationRepository by inject()
    val authorization: AuthorizationService by inject()

    route("/gyms/{gymId}") {
        get("/plans/{planId}/structure") {
            val gymId = call.gymIdOrBadRequest() ?: return@get
            val planId = call.planIdOrBadRequest() ?: return@get
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.isSuperAdmin(actorId) && authorization.gymRole(actorId, gymId) == null) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }
            repository.getStructure(gymId, planId)?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound)
        }

        post("/plans") {
            val gymId = call.gymIdOrBadRequest() ?: return@post
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@post call.respond(HttpStatusCode.Forbidden)
            try {
                call.respond(HttpStatusCode.Created, repository.create(gymId, call.receive<SavePlanificationRequest>()))
            } catch (error: DomainNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (error: DomainConflictException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put("/plans/{planId}") {
            val gymId = call.gymIdOrBadRequest() ?: return@put
            val planId = call.planIdOrBadRequest() ?: return@put
            val actorId = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@put call.respond(HttpStatusCode.Forbidden)
            try {
                call.respond(HttpStatusCode.OK, repository.replace(gymId, planId, call.receive<SavePlanificationRequest>()))
            } catch (error: DomainNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (error: DomainConflictException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/plans/{planId}/clone") {
            val gymId = call.gymIdOrBadRequest() ?: return@post
            val planId = call.planIdOrBadRequest() ?: return@post
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@post call.respond(HttpStatusCode.Forbidden)
            try {
                call.respond(HttpStatusCode.Created, repository.clone(gymId, planId, call.receive<ClonePlanificationRequest>()))
            } catch (error: DomainNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/plans/{planId}/assignments") {
            val gymId = call.gymIdOrBadRequest() ?: return@post
            val planId = call.planIdOrBadRequest() ?: return@post
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@post call.respond(HttpStatusCode.Forbidden)
            try {
                call.respond(HttpStatusCode.Created, repository.assign(gymId, planId, call.receive<AssignPlanificationRequest>()))
            } catch (error: DomainNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (error: DomainConflictException) {
                call.respond(HttpStatusCode.Conflict)
            }
        }

        get("/members/{userId}/plans") {
            val gymId = call.gymIdOrBadRequest() ?: return@get
            val userId = call.parameters["userId"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@get call.respond(HttpStatusCode.Forbidden)
            try {
                call.respond(repository.getMemberPlans(gymId, userId))
            } catch (error: DomainNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.gymIdOrBadRequest(): Long? =
    parameters["gymId"]?.toLongOrNull() ?: run {
        respond(HttpStatusCode.BadRequest)
        null
    }

private suspend fun io.ktor.server.application.ApplicationCall.planIdOrBadRequest(): Long? =
    parameters["planId"]?.toLongOrNull() ?: run {
        respond(HttpStatusCode.BadRequest)
        null
    }
