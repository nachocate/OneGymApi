package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Evaluation
import com.concatstudio.onegym.respository.EvaluationRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.evaluationRouting() {
    val repository: EvaluationRepository by inject()
    route("/evaluations") {
        get { call.respond(repository.getEvaluations()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getEvaluationById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createEvaluation(call.receive<Evaluation>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getEvaluationById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateEvaluation(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteEvaluationById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
