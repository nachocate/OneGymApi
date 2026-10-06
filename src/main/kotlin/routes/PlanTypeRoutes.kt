package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.PlanType
import com.concatstudio.onegym.respository.PlanTypeRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.planTypeRouting() {
    val repository: PlanTypeRepository by inject()
    route("/plan-types") {
        get { call.respond(repository.getPlanTypes()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getPlanTypeById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createPlanType(call.receive<PlanType>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getPlanTypeById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updatePlanType(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deletePlanTypeById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
