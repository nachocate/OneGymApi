package com.concatstudio.onegym.routes

import com.concatstudio.onegym.mappers.toResponse
import com.concatstudio.onegym.respository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.profileRouting() {
    val userRepository: UserRepository by inject()

    get("/profile/me") {
        val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val user = userRepository.getUserById(userId)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(user.toResponse())
    }
}
