package com.concatstudio.onegym.routes
import com.concatstudio.onegym.model.Block
import com.concatstudio.onegym.respository.BlockRepository
import com.concatstudio.onegym.security.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
fun Route.blockRouting() { val repo: BlockRepository by inject(); val auth: AuthorizationService by inject(); route("/blocks") {
    get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repo.getBlocks()) }
    get("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@get call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForBlock(id)?:return@get call.respond(HttpStatusCode.NotFound); val actor=call.authenticatedUserId()?:return@get call.respond(HttpStatusCode.Unauthorized); if(!auth.isSuperAdmin(actor)&&auth.gymRole(actor,gym)==null) call.respond(HttpStatusCode.Forbidden) else repo.getBlockById(id)?.let{call.respond(it)} }
    post { val item=call.receive<Block>(); val actor=call.authenticatedUserId()?:return@post call.respond(HttpStatusCode.Unauthorized); val gym=auth.gymForDay(item.dayId)?:return@post call.respond(HttpStatusCode.NotFound); if(!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.createBlock(item);call.respond(HttpStatusCode.Created)} }
    put("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@put call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForBlock(id)?:return@put call.respond(HttpStatusCode.NotFound); val item=call.receive<Block>(); val actor=call.authenticatedUserId()?:return@put call.respond(HttpStatusCode.Unauthorized); if(auth.gymForDay(item.dayId)!=gym||!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.updateBlock(id,item);call.respond(HttpStatusCode.NoContent)} }
    delete("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@delete call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForBlock(id)?:return@delete call.respond(HttpStatusCode.NotFound); val actor=call.authenticatedUserId()?:return@delete call.respond(HttpStatusCode.Unauthorized); if(!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.deleteBlockById(id);call.respond(HttpStatusCode.NoContent)} }
} }
