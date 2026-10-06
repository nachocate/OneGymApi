package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.UserPlan
import com.concatstudio.onegym.respository.UserPlanRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userPlanRouting() {
    val repository: UserPlanRepository by inject()
    route("/user-plans") {
        get { call.respond(repository.getUserPlans()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getUserPlanById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createUserPlan(call.receive<UserPlan>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getUserPlanById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateUserPlan(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteUserPlanById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
