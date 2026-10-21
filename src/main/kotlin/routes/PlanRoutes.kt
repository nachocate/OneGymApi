package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Plan
import com.concatstudio.onegym.respository.PlanRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.planRouting() {
    val repository: PlanRepository by inject(); val auth: AuthorizationService by inject()
    route("/plans") {
        get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repository.getPlans()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest); val gym = auth.gymForPlan(id) ?: return@get call.respond(HttpStatusCode.NotFound); val actor = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized); if (!auth.isSuperAdmin(actor) && auth.gymRole(actor, gym) == null) call.respond(HttpStatusCode.Forbidden) else repository.getPlanById(id)?.let { call.respond(it) } }
        post { val item = call.receive<Plan>(); val actor = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized); if (!auth.canManageTraining(actor, item.gymId)) call.respond(HttpStatusCode.Forbidden) else { repository.createPlan(item); call.respond(HttpStatusCode.Created) } }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest); val gym = auth.gymForPlan(id) ?: return@put call.respond(HttpStatusCode.NotFound); val actor = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized); val item = call.receive<Plan>(); if (gym != item.gymId || !auth.canManageTraining(actor, gym)) call.respond(HttpStatusCode.Forbidden) else { repository.updatePlan(id, item); call.respond(HttpStatusCode.NoContent) } }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest); val gym = auth.gymForPlan(id) ?: return@delete call.respond(HttpStatusCode.NotFound); val actor = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized); if (!auth.canManageTraining(actor, gym)) call.respond(HttpStatusCode.Forbidden) else { repository.deletePlanById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
