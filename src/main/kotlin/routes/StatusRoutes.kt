package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.UserStatusResponse
import com.concatstudio.onegym.respository.UserStatusRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.statusRouting() {
    val statusRepository: UserStatusRepository by inject()

    get("/status") {
        val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val subscriptions = statusRepository.getActiveGymSubscriptions(userId)
        call.respond(UserStatusResponse(gymCount = subscriptions.size, gyms = subscriptions))
    }
}
