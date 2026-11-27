package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.SaveCurrentPlanActivityRequest
import com.concatstudio.onegym.model.toModel
import com.concatstudio.onegym.respository.CurrentPlanActivityRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.currentPlanActivityRouting() {
    val repository: CurrentPlanActivityRepository by inject(); val auth: AuthorizationService by inject()
    route("/current-plan-activities") {
        get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repository.getCurrentPlanActivities()) }
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val gym = auth.gymForCurrentPlanActivity(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val targetUser = auth.userForCurrentPlanActivity(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!auth.canAccessUserThroughGym(actor, targetUser) || (!auth.isSuperAdmin(actor) && auth.gymRole(actor, gym) == null)) call.respond(HttpStatusCode.Forbidden)
            else repository.getCurrentPlanActivityById(id)?.let { call.respond(it) }
        }
        post {
            val item = call.receive<SaveCurrentPlanActivityRequest>().toModel(); val actor = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val gym = auth.gymForUserPlan(item.userPlanId) ?: return@post call.respond(HttpStatusCode.NotFound)
            val targetUser = auth.userForUserPlan(item.userPlanId) ?: return@post call.respond(HttpStatusCode.NotFound)
            if (!auth.canManageTraining(actor, gym) || !auth.hasActiveGymMembership(targetUser, gym)) call.respond(HttpStatusCode.Forbidden)
            else { repository.createCurrentPlanActivity(item); call.respond(HttpStatusCode.Created) }
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val gym = auth.gymForCurrentPlanActivity(id) ?: return@put call.respond(HttpStatusCode.NotFound)
            val item = call.receive<SaveCurrentPlanActivityRequest>().toModel(); val candidateGym = auth.gymForUserPlan(item.userPlanId) ?: return@put call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (candidateGym != gym || !auth.canManageTraining(actor, gym)) call.respond(HttpStatusCode.Forbidden)
            else { repository.updateCurrentPlanActivity(id, item); call.respond(HttpStatusCode.NoContent) }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val gym = auth.gymForCurrentPlanActivity(id) ?: return@delete call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (!auth.canManageTraining(actor, gym)) call.respond(HttpStatusCode.Forbidden)
            else { repository.deleteCurrentPlanActivityById(id); call.respond(HttpStatusCode.NoContent) }
        }
    }
}
