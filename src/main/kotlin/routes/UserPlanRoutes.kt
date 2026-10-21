package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.SaveUserPlanRequest
import com.concatstudio.onegym.model.toModel
import com.concatstudio.onegym.respository.UserPlanRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import com.concatstudio.onegym.security.isSuperAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userPlanRouting() {
    val repository: UserPlanRepository by inject(); val auth: AuthorizationService by inject()
    route("/user-plans") {
        get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repository.getUserPlans()) }
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val gym = auth.gymForUserPlan(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val targetUser = auth.userForUserPlan(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!auth.canAccessUserThroughGym(actor, targetUser) || (!auth.isSuperAdmin(actor) && auth.gymRole(actor, gym) == null)) call.respond(HttpStatusCode.Forbidden)
            else repository.getUserPlanById(id)?.let { call.respond(it) }
        }
        post {
            val item = call.receive<SaveUserPlanRequest>().toModel(); val actor = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val gym = auth.gymForPlan(item.planId) ?: return@post call.respond(HttpStatusCode.NotFound)
            if (!auth.canManageTraining(actor, gym) || !auth.hasActiveGymMembership(item.userId, gym)) call.respond(HttpStatusCode.Forbidden)
            else { repository.createUserPlan(item); call.respond(HttpStatusCode.Created) }
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val gym = auth.gymForUserPlan(id) ?: return@put call.respond(HttpStatusCode.NotFound)
            val targetUser = auth.userForUserPlan(id) ?: return@put call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val item = call.receive<SaveUserPlanRequest>().toModel(); val newGym = auth.gymForPlan(item.planId) ?: return@put call.respond(HttpStatusCode.NotFound)
            if (item.userId != targetUser || newGym != gym || !auth.canManageTraining(actor, gym) || !auth.hasActiveGymMembership(targetUser, gym)) call.respond(HttpStatusCode.Forbidden)
            else { repository.updateUserPlan(id, item); call.respond(HttpStatusCode.NoContent) }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val gym = auth.gymForUserPlan(id) ?: return@delete call.respond(HttpStatusCode.NotFound)
            val actor = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (!auth.canManageTraining(actor, gym)) call.respond(HttpStatusCode.Forbidden)
            else { repository.deleteUserPlanById(id); call.respond(HttpStatusCode.NoContent) }
        }
    }
}
