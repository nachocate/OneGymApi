package com.concatstudio.onegym.routes

import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRouting(databaseReady: suspend () -> Boolean = { Database.isReady() }) {
    get("/health/ready") {
        if (databaseReady()) {
            call.respond(mapOf("status" to "ready"))
        } else {
            call.application.log.warn("PostgreSQL readiness check failed")
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ApiError("service_unavailable", "Service temporarily unavailable")
            )
        }
    }
}
