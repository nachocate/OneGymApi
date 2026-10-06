package com.concatstudio.onegym.routes

import com.concatstudio.onegym.model.UserTest
import com.concatstudio.onegym.respository.UserTestRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userTestRouting() {
    val repository: UserTestRepository by inject()
    route("/user-tests") {
        get { call.respond(repository.getUserTests()) }
        get("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else repository.getUserTestById(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) }
        post { repository.createUserTest(call.receive<UserTest>()); call.respond(HttpStatusCode.Created) }
        put("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) { call.respond(HttpStatusCode.BadRequest); return@put }; if (repository.getUserTestById(id) == null) { call.respond(HttpStatusCode.NotFound); return@put }; repository.updateUserTest(id, call.receive()); call.respond(HttpStatusCode.NoContent) }
        delete("/{id}") { val id = call.parameters["id"]?.toLongOrNull(); if (id == null) call.respond(HttpStatusCode.BadRequest) else { repository.deleteUserTestById(id); call.respond(HttpStatusCode.NoContent) } }
    }
}
