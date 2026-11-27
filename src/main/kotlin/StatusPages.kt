package com.concatstudio.onegym

import com.concatstudio.onegym.data.DomainConflictException
import com.concatstudio.onegym.model.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import java.sql.SQLException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<DomainConflictException> { call, error -> call.respond(HttpStatusCode.Conflict, ApiError("conflict", error.message ?: "Conflict")) }
        exception<IllegalArgumentException> { call, error -> call.respond(HttpStatusCode.BadRequest, ApiError("invalid_request", error.message ?: "Invalid request")) }
        exception<Throwable> { call, error ->
            when (error.sqlState()) {
                "23505" -> call.respond(HttpStatusCode.Conflict, ApiError("conflict", "The resource conflicts with existing data"))
                "23503", "23514" -> call.respond(HttpStatusCode.UnprocessableEntity, ApiError("invalid_state", "The request violates a business rule"))
                else -> call.respond(HttpStatusCode.InternalServerError, ApiError("internal_error", "Unexpected server error"))
            }
        }
    }
}

private fun Throwable.sqlState(): String? = generateSequence(this) { it.cause }
    .filterIsInstance<SQLException>()
    .firstOrNull()
    ?.sqlState
