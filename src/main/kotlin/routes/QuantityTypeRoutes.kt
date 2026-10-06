package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.QuantityType
import com.concatstudio.onegym.respository.QuantityTypeRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.quantityTypeRouting() {
    val repository: QuantityTypeRepository by inject()
    route("/quantity-types") {
        get { call.respond(repository.getQuantityTypes()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getQuantityTypeById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createQuantityType(call.receive<QuantityType>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getQuantityTypeById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateQuantityType(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteQuantityTypeById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
