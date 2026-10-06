package com.concatstudio.onegym.routes

import com.concatstudio.onegym.mappers.toResponse
import com.concatstudio.onegym.model.LoginRequest
import com.concatstudio.onegym.respository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.authenticationRouting() {
    val userRepository: UserRepository by inject()

    route("/login") {
        post {
            val request = call.receive<LoginRequest>()
            val user = userRepository.getUsers()
                .firstOrNull { it.email.equals(request.email, ignoreCase = true) }

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val authenticated = userRepository.checkPassword(
                userId = user.id,
                password = request.password
            )

            if (authenticated) {
                call.respond(user.toResponse())
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
