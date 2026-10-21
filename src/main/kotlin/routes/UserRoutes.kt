package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.ChangePasswordRequest
import com.concatstudio.onegym.model.User
import com.concatstudio.onegym.model.UpdateUserProfileRequest
import com.concatstudio.onegym.mappers.toResponse
import com.concatstudio.onegym.respository.UserRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/** Global users are never managed by a gym's membership CRUD. */
fun Route.userRouting() {
    val userRepository: UserRepository by inject()
    val authorizationService: AuthorizationService by inject()
    route("/users") {
        get {
            if (!call.isSuperAdmin(authorizationService)) return@get call.respond(HttpStatusCode.Forbidden)
            call.respond(userRepository.getUsers().map { it.toResponse() })
        }
        get("/{id}") {
            val userId = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (actorId != userId && !authorizationService.isSuperAdmin(actorId)) return@get call.respond(HttpStatusCode.Forbidden)
            userRepository.getUserById(userId)?.let { call.respond(it.toResponse()) } ?: call.respond(HttpStatusCode.NotFound)
        }
        post {
            if (!call.isSuperAdmin(authorizationService)) return@post call.respond(HttpStatusCode.Forbidden)
            userRepository.createUser(call.receive<User>())
            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val userId = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (actorId != userId && !authorizationService.isSuperAdmin(actorId)) return@put call.respond(HttpStatusCode.Forbidden)
            if (userRepository.getUserById(userId) == null) return@put call.respond(HttpStatusCode.NotFound)
            userRepository.updateUserProfile(userId, call.receive<UpdateUserProfileRequest>())
            call.respond(HttpStatusCode.NoContent)
        }
        put("/{id}/password") {
            val userId = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (actorId != userId) return@put call.respond(HttpStatusCode.Forbidden)
            val request = call.receive<ChangePasswordRequest>()
            if (userRepository.changePassword(userId, request.currentPassword, request.newPassword)) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.Unauthorized)
        }
        delete("/{id}") {
            val userId = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (actorId != userId && !authorizationService.isSuperAdmin(actorId)) return@delete call.respond(HttpStatusCode.Forbidden)
            userRepository.deleteUserById(userId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
