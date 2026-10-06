package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.Register
import com.concatstudio.onegym.respository.RegisterRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.registerRouting() {
    val repository: RegisterRepository by inject()
    route("/registers") {
        get { call.respond(repository.getRegisters()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getRegisterById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createRegister(call.receive<Register>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getRegisterById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateRegister(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteRegisterById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
