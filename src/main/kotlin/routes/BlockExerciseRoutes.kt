package com.concatstudio.onegym.routes
import com.concatstudio.onegym.model.BlockExercise
import com.concatstudio.onegym.respository.BlockExerciseRepository
import com.concatstudio.onegym.security.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
fun Route.blockExerciseRouting() { val repo: BlockExerciseRepository by inject(); val auth: AuthorizationService by inject(); route("/block-exercises") {
    get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repo.getBlockExercises()) }
    get("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@get call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForBlockExercise(id)?:return@get call.respond(HttpStatusCode.NotFound); val actor=call.authenticatedUserId()?:return@get call.respond(HttpStatusCode.Unauthorized); if(!auth.isSuperAdmin(actor)&&auth.gymRole(actor,gym)==null) call.respond(HttpStatusCode.Forbidden) else repo.getBlockExerciseById(id)?.let{call.respond(it)} }
    post { val item=call.receive<BlockExercise>(); val actor=call.authenticatedUserId()?:return@post call.respond(HttpStatusCode.Unauthorized); val gym=auth.gymForBlock(item.blockId)?:return@post call.respond(HttpStatusCode.NotFound); if(!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.createBlockExercise(item);call.respond(HttpStatusCode.Created)} }
    put("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@put call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForBlockExercise(id)?:return@put call.respond(HttpStatusCode.NotFound); val item=call.receive<BlockExercise>(); val actor=call.authenticatedUserId()?:return@put call.respond(HttpStatusCode.Unauthorized); if(auth.gymForBlock(item.blockId)!=gym||!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.updateBlockExercise(id,item);call.respond(HttpStatusCode.NoContent)} }
    delete("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@delete call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForBlockExercise(id)?:return@delete call.respond(HttpStatusCode.NotFound); val actor=call.authenticatedUserId()?:return@delete call.respond(HttpStatusCode.Unauthorized); if(!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.deleteBlockExerciseById(id);call.respond(HttpStatusCode.NoContent)} }
} }
