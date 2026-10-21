package com.concatstudio.onegym.routes
import com.concatstudio.onegym.model.Day
import com.concatstudio.onegym.respository.DayRepository
import com.concatstudio.onegym.security.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
fun Route.dayRouting() { val repo: DayRepository by inject(); val auth: AuthorizationService by inject(); route("/days") {
    get { if (!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repo.getDays()) }
    get("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@get call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForDay(id)?:return@get call.respond(HttpStatusCode.NotFound); val actor=call.authenticatedUserId()?:return@get call.respond(HttpStatusCode.Unauthorized); if(!auth.isSuperAdmin(actor)&&auth.gymRole(actor,gym)==null) call.respond(HttpStatusCode.Forbidden) else repo.getDayById(id)?.let{call.respond(it)} }
    post { val item=call.receive<Day>(); val actor=call.authenticatedUserId()?:return@post call.respond(HttpStatusCode.Unauthorized); val gym=auth.gymForWeek(item.weekId)?:return@post call.respond(HttpStatusCode.NotFound); if(!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.createDay(item);call.respond(HttpStatusCode.Created)} }
    put("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@put call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForDay(id)?:return@put call.respond(HttpStatusCode.NotFound); val item=call.receive<Day>(); val actor=call.authenticatedUserId()?:return@put call.respond(HttpStatusCode.Unauthorized); if(auth.gymForWeek(item.weekId)!=gym||!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.updateDay(id,item);call.respond(HttpStatusCode.NoContent)} }
    delete("/{id}") { val id=call.parameters["id"]?.toLongOrNull()?:return@delete call.respond(HttpStatusCode.BadRequest); val gym=auth.gymForDay(id)?:return@delete call.respond(HttpStatusCode.NotFound); val actor=call.authenticatedUserId()?:return@delete call.respond(HttpStatusCode.Unauthorized); if(!auth.canManageTraining(actor,gym)) call.respond(HttpStatusCode.Forbidden) else {repo.deleteDayById(id);call.respond(HttpStatusCode.NoContent)} }
} }
