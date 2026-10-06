package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Plan
import com.concatstudio.onegym.respository.PlanRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.planRouting() {
    val repository: PlanRepository by inject()
    route("/plans") {
        get { call.respond(repository.getPlans()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getPlanById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createPlan(call.receive<Plan>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getPlanById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updatePlan(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deletePlanById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
