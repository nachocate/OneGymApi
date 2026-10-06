package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Exercise
import com.concatstudio.onegym.respository.ExerciseRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.exerciseRouting() {
    val repository: ExerciseRepository by inject()
    route("/exercises") {
        get { call.respond(repository.getExercises()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getExerciseById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createExercise(call.receive<Exercise>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getExerciseById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateExercise(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteExerciseById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
