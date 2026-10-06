package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Block
import com.concatstudio.onegym.respository.BlockRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.blockRouting() {
    val repository: BlockRepository by inject()
    route("/blocks") {
        get { call.respond(repository.getBlocks()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getBlockById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createBlock(call.receive<Block>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getBlockById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateBlock(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteBlockById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
