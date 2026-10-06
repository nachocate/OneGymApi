package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.User
import com.concatstudio.onegym.model.ChangePasswordRequest
import com.concatstudio.onegym.mappers.toResponse
import com.concatstudio.onegym.respository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.userRouting(
) {
    val userRepository: UserRepository by inject()
    route("/users") {
        get{
            val response = userRepository.getUsers().map { it.toResponse() }
            call.respond(response)

        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id != null) {
                val user = userRepository.getUserById(id)
                if (user != null) {
                    call.respond(user.toResponse())
                }else{
                    call.respond(HttpStatusCode.NotFound)
                }
            }else{
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post() {
            val request = call.receive<User>()
            userRepository.createUser(request)
            call.respond(HttpStatusCode.Created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (userRepository.getUserById(id) == null) {
                call.respond(HttpStatusCode.NotFound)
                return@put
            }
            userRepository.updateUser(id, call.receive())
            call.respond(HttpStatusCode.NoContent)
        }

        put("/{id}/password") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val request = call.receive<ChangePasswordRequest>()
            val changed = userRepository.changePassword(
                userId = id,
                currentPassword = request.currentPassword,
                newPassword = request.newPassword
            )
            if (changed) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id != null) {
                userRepository.deleteUserById(id)
                call.respond(HttpStatusCode.NoContent)
            }else{
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
