package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.BlockExercise
import com.concatstudio.onegym.respository.BlockExerciseRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.blockExerciseRouting() {
    val repository: BlockExerciseRepository by inject()
    route("/block-exercises") {
        get { call.respond(repository.getBlockExercises()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getBlockExerciseById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createBlockExercise(call.receive<BlockExercise>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getBlockExerciseById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateBlockExercise(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteBlockExerciseById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
