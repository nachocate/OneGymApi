package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.ActivePlansResponse
import com.concatstudio.onegym.respository.ActivePlanRepository
import com.concatstudio.onegym.respository.UserStatusRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.activePlanRouting() {
    val activePlanRepository: ActivePlanRepository by inject()
    val statusRepository: UserStatusRepository by inject()

    get("/plan/{gymId}/me") {
        val gymId = call.parameters["gymId"]?.toLongOrNull()
        val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        if (gymId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }
        if (!statusRepository.hasActiveGymSubscription(userId, gymId)) {
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        call.respond(ActivePlansResponse(activePlanRepository.getActivePlans(userId, gymId)))
    }
}
