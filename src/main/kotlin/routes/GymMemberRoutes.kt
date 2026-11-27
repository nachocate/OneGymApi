package com.concatstudio.onegym.routes

import com.concatstudio.onegym.data.DomainConflictException
import com.concatstudio.onegym.data.DomainNotFoundException
import com.concatstudio.onegym.data.GymManagementService
import com.concatstudio.onegym.model.AssignGymMemberRequest
import com.concatstudio.onegym.model.RegisterGymMemberRequest
import com.concatstudio.onegym.respository.UserGymRepository
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.GymRole
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.gymMemberRouting() {
    val managementService: GymManagementService by inject()
    val authorizationService: AuthorizationService by inject()
    val membershipRepository: UserGymRepository by inject()
    route("/gyms/{gymId}/members") {
        get {
            val gymId = call.parameters["gymId"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (!authorizationService.canManageTraining(actorId, gymId)) return@get call.respond(HttpStatusCode.Forbidden)
            val page = call.pageRequest() ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(membershipRepository.getGymMembersByGymId(gymId, page.limit, page.offset))
        }
        post {
            val gymId = call.parameters["gymId"]?.toLongOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<AssignGymMemberRequest>()
            val targetRole = GymRole.fromName(request.roleName) ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (!authorizationService.canAssignGymRole(actorId, gymId, targetRole)) return@post call.respond(HttpStatusCode.Forbidden)
            try { call.respond(HttpStatusCode.Created, managementService.assignMember(gymId, request)) }
            catch (error: DomainNotFoundException) { call.respond(HttpStatusCode.NotFound) }
            catch (error: DomainConflictException) { call.respond(HttpStatusCode.Conflict) }
            catch (error: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest) }
        }
        post("/registration") {
            val gymId = call.parameters["gymId"]?.toLongOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<RegisterGymMemberRequest>()
            val targetRole = GymRole.fromName(request.roleName) ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (!authorizationService.canAssignGymRole(actorId, gymId, targetRole)) return@post call.respond(HttpStatusCode.Forbidden)
            try { call.respond(HttpStatusCode.Created, managementService.registerMember(gymId, request)) }
            catch (error: DomainNotFoundException) { call.respond(HttpStatusCode.NotFound) }
            catch (error: DomainConflictException) { call.respond(HttpStatusCode.Conflict) }
            catch (error: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest) }
        }
        delete("/{userId}") {
            val gymId = call.parameters["gymId"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val targetUserId = call.parameters["userId"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val targetRole = authorizationService.gymRole(targetUserId, gymId) ?: return@delete call.respond(HttpStatusCode.NotFound)
            if (actorId != targetUserId && !authorizationService.canAssignGymRole(actorId, gymId, targetRole)) return@delete call.respond(HttpStatusCode.Forbidden)
            try { managementService.removeMember(gymId, targetUserId); call.respond(HttpStatusCode.NoContent) }
            catch (error: DomainNotFoundException) { call.respond(HttpStatusCode.NotFound) }
        }
    }
}
