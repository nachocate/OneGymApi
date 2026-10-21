package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.UserRole
import com.concatstudio.onegym.respository.UserRoleRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoleRouting() {
    val repository: UserRoleRepository by inject(); val auth: AuthorizationService by inject()
    route("/user-roles") {
        get { call.respond(repository.getUserRoles()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getUserRoleById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else { repository.createUserRole(call.receive<UserRole>()); call.respond(HttpStatusCode.Created) } }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (!call.isSuperAdmin(auth)) { call.respond(HttpStatusCode.Forbidden); return@put }; if (repository.getUserRoleById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateUserRole(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else { repository.deleteUserRoleById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
