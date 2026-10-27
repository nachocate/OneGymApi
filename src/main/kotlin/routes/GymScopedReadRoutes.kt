package com.concatstudio.onegym.routes

import com.concatstudio.onegym.respository.ExerciseRepository
import com.concatstudio.onegym.respository.GymBusinessHourRepository
import com.concatstudio.onegym.respository.GymCoachRepository
import com.concatstudio.onegym.respository.PlanRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.gymScopedReadRouting() {
    val authorization: AuthorizationService by inject()
    val coaches: GymCoachRepository by inject()
    val hours: GymBusinessHourRepository by inject()
    val plans: PlanRepository by inject()
    val exercises: ExerciseRepository by inject()

    fun Route.memberRead(path: String, response: (Long, Int, Long) -> Any) = get(path) {
        val gymId = call.parameters["gymId"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
        if (!authorization.isSuperAdmin(actorId) && authorization.gymRole(actorId, gymId) == null) return@get call.respond(HttpStatusCode.Forbidden)
        val page = call.pageRequest() ?: return@get call.respond(HttpStatusCode.BadRequest)
        call.respond(response(gymId, page.limit, page.offset))
    }

    route("/gyms/{gymId}") {
        memberRead("/coaches") { gymId, limit, offset -> coaches.getGymCoachesByGymId(gymId, limit, offset) }
        memberRead("/business-hours") { gymId, limit, offset -> hours.getGymBusinessHoursByGymId(gymId, limit, offset) }
        memberRead("/plans") { gymId, limit, offset -> plans.getPlanSummariesByGymId(gymId, limit, offset) }
        memberRead("/exercises") { gymId, limit, offset -> exercises.getExercisesAvailableToGym(gymId, limit, offset) }
    }
}
