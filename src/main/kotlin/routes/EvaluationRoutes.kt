package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Evaluation
import com.concatstudio.onegym.respository.EvaluationRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.evaluationRouting() {
    val repository: EvaluationRepository by inject()
    val authorization: AuthorizationService by inject()
    route("/evaluations") {
        get {
            val userId = call.request.queryParameters["userId"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val actor = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canAccessUserThroughGym(actor, userId)) return@get call.respond(HttpStatusCode.Forbidden)
            call.respond(repository.getEvaluationsByUserId(userId))
        }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest); val item = repository.getEvaluationById(id) ?: return@get call.respond(HttpStatusCode.NotFound); val actor = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized); if (!authorization.canAccessUserThroughGym(actor, item.userId)) call.respond(HttpStatusCode.Forbidden) else call.respond(item) }
        post { val item = call.receive<Evaluation>(); val actor = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized); if (!authorization.canManageUserTrainingData(actor, item.userId)) call.respond(HttpStatusCode.Forbidden) else { repository.createEvaluation(item); call.respond(HttpStatusCode.Created) } }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest); val current = repository.getEvaluationById(id) ?: return@put call.respond(HttpStatusCode.NotFound); val actor = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized); val item = call.receive<Evaluation>(); if (!authorization.canManageUserTrainingData(actor, current.userId) || current.userId != item.userId) call.respond(HttpStatusCode.Forbidden) else { repository.updateEvaluation(id, item); call.respond(HttpStatusCode.NoContent) } }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest); val item = repository.getEvaluationById(id) ?: return@delete call.respond(HttpStatusCode.NotFound); val actor = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized); if (!authorization.canManageUserTrainingData(actor, item.userId)) call.respond(HttpStatusCode.Forbidden) else { repository.deleteEvaluationById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
