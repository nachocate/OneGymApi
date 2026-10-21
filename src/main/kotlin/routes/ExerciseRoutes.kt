package com.concatstudio.onegym.routes
import com.concatstudio.onegym.model.Exercise
import com.concatstudio.onegym.respository.ExerciseRepository
import com.concatstudio.onegym.security.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
fun Route.exerciseRouting(){val repo:ExerciseRepository by inject();val auth:AuthorizationService by inject();route("/exercises"){
 get{if(!call.isSuperAdmin(auth))call.respond(HttpStatusCode.Forbidden)else call.respond(repo.getExercises())}
 get("/{id}"){val id=call.parameters["id"]?.toLongOrNull()?:return@get call.respond(HttpStatusCode.BadRequest);val e=repo.getExerciseById(id)?:return@get call.respond(HttpStatusCode.NotFound);val a=call.authenticatedUserId()?:return@get call.respond(HttpStatusCode.Unauthorized);if(e.gymId!=null&&!auth.isSuperAdmin(a)&&auth.gymRole(a,e.gymId)==null)call.respond(HttpStatusCode.Forbidden)else call.respond(e)}
 post{val e=call.receive<Exercise>();if(e.gymId==null&&!e.isVisibleGlobal)return@post call.respond(HttpStatusCode.BadRequest);val a=call.authenticatedUserId()?:return@post call.respond(HttpStatusCode.Unauthorized);if((e.gymId==null&&!auth.isSuperAdmin(a))||(e.gymId!=null&&!auth.canManageTraining(a,e.gymId)))call.respond(HttpStatusCode.Forbidden)else{repo.createExercise(e);call.respond(HttpStatusCode.Created)}}
 put("/{id}"){val id=call.parameters["id"]?.toLongOrNull()?:return@put call.respond(HttpStatusCode.BadRequest);val old=repo.getExerciseById(id)?:return@put call.respond(HttpStatusCode.NotFound);val e=call.receive<Exercise>();if(e.gymId==null&&!e.isVisibleGlobal)return@put call.respond(HttpStatusCode.BadRequest);val a=call.authenticatedUserId()?:return@put call.respond(HttpStatusCode.Unauthorized);if(old.gymId!=e.gymId||if(old.gymId==null)!auth.isSuperAdmin(a)else !auth.canManageTraining(a,old.gymId))call.respond(HttpStatusCode.Forbidden)else{repo.updateExercise(id,e);call.respond(HttpStatusCode.NoContent)}}
 delete("/{id}"){val id=call.parameters["id"]?.toLongOrNull()?:return@delete call.respond(HttpStatusCode.BadRequest);val e=repo.getExerciseById(id)?:return@delete call.respond(HttpStatusCode.NotFound);val a=call.authenticatedUserId()?:return@delete call.respond(HttpStatusCode.Unauthorized);if(if(e.gymId==null)!auth.isSuperAdmin(a)else !auth.canManageTraining(a,e.gymId))call.respond(HttpStatusCode.Forbidden)else{repo.deleteExerciseById(id);call.respond(HttpStatusCode.NoContent)}}
} }
