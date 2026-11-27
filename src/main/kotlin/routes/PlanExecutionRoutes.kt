package com.concatstudio.onegym.routes

import com.concatstudio.onegym.data.DomainConflictException
import com.concatstudio.onegym.data.DomainNotFoundException
import com.concatstudio.onegym.model.StartPlanExecutionRequest
import com.concatstudio.onegym.model.UpdatePlanExecutionItemRequest
import com.concatstudio.onegym.respository.PlanExecutionRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.LocalDate

/** End-user tracking of actual routine execution. It is not an attendance system. */
fun Route.planExecutionRouting() {
    val repository: PlanExecutionRepository by inject()
    val authorization: AuthorizationService by inject()

    route("/gyms/{gymId}") {
        post("/me/plan-executions") {
            val gymId = call.executionGymId() ?: return@post
            val userId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.hasActiveGymMembership(userId, gymId)) return@post call.respond(HttpStatusCode.Forbidden)
            val request = call.receive<StartPlanExecutionRequest>()
            call.executionResult {
                val result = repository.start(gymId, userId, request.userPlanId, request.planDayId)
                call.respond(if (result.created) HttpStatusCode.Created else HttpStatusCode.OK, result.execution)
            }
        }

        get("/me/plan-executions") {
            val gymId = call.executionGymId() ?: return@get
            val userId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.hasActiveGymMembership(userId, gymId)) return@get call.respond(HttpStatusCode.Forbidden)
            val range = call.executionDateRange() ?: return@get
            call.executionResult { call.respond(repository.list(gymId, userId, range.first, range.second)) }
        }

        get("/me/plan-executions/{executionId}") {
            val gymId = call.executionGymId() ?: return@get
            val userId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.hasActiveGymMembership(userId, gymId)) return@get call.respond(HttpStatusCode.Forbidden)
            val executionId = call.executionId() ?: return@get
            call.executionResult {
                val execution = repository.get(gymId, executionId) ?: return@executionResult call.respond(HttpStatusCode.NotFound)
                if (authorization.userForUserPlan(execution.userPlanId) != userId) call.respond(HttpStatusCode.Forbidden) else call.respond(execution)
            }
        }

        patch("/me/plan-executions/{executionId}/items/{itemId}") {
            val gymId = call.executionGymId() ?: return@patch
            val userId = call.authenticatedUserId() ?: return@patch call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.hasActiveGymMembership(userId, gymId)) return@patch call.respond(HttpStatusCode.Forbidden)
            val executionId = call.executionId() ?: return@patch
            val itemId = call.parameters["itemId"]?.toLongOrNull() ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<UpdatePlanExecutionItemRequest>()
            call.executionResult {
                val execution = repository.get(gymId, executionId) ?: return@executionResult call.respond(HttpStatusCode.NotFound)
                if (authorization.userForUserPlan(execution.userPlanId) != userId) return@executionResult call.respond(HttpStatusCode.Forbidden)
                call.respond(repository.updateItem(gymId, executionId, itemId, request.completed))
            }
        }

        get("/members/{userId}/plan-executions") {
            val gymId = call.executionGymId() ?: return@get
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@get call.respond(HttpStatusCode.Forbidden)
            val userId = call.parameters["userId"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val range = call.executionDateRange() ?: return@get
            call.executionResult { call.respond(repository.list(gymId, userId, range.first, range.second)) }
        }

        get("/plan-execution-adherence") {
            val gymId = call.executionGymId() ?: return@get
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@get call.respond(HttpStatusCode.Forbidden)
            val range = call.executionDateRange() ?: return@get
            call.executionResult { call.respond(repository.summaries(gymId, range.first, range.second)) }
        }
    }
}

private suspend fun ApplicationCall.executionGymId(): Long? = parameters["gymId"]?.toLongOrNull() ?: run { respond(HttpStatusCode.BadRequest); null }
private suspend fun ApplicationCall.executionId(): Long? = parameters["executionId"]?.toLongOrNull() ?: run { respond(HttpStatusCode.BadRequest); null }
private suspend fun ApplicationCall.executionDateRange(): Pair<LocalDate, LocalDate>? {
    val from = request.queryParameters["from"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val to = request.queryParameters["to"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    return if (from == null || to == null) { respond(HttpStatusCode.BadRequest); null } else from to to
}
private suspend fun ApplicationCall.executionResult(block: suspend () -> Unit) {
    try { block() } catch (_: DomainNotFoundException) { respond(HttpStatusCode.NotFound) }
    catch (_: DomainConflictException) { respond(HttpStatusCode.Conflict) }
    catch (_: IllegalArgumentException) { respond(HttpStatusCode.BadRequest) }
}
