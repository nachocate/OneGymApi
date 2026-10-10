package com.concatstudio.onegym

import com.concatstudio.onegym.modules.appDiModules
import io.ktor.server.application.Application
import io.ktor.server.application.install
import com.concatstudio.onegym.security.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject

import org.koin.ktor.plugin.Koin

fun Application.rootModule() {
    val jwtService: JwtService by inject()

    install(CORS) {
        allowHost("localhost:5173", schemes = listOf("http"))
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(Koin) {
        configureSerialization()
        modules(appDiModules)

    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "OneGym API"
            verifier(jwtService.verifier)
            validate { credential ->
                credential.payload.subject?.toLongOrNull()?.let { io.ktor.server.auth.jwt.JWTPrincipal(credential.payload) }
            }
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized) }
        }
    }

    configureRouting()
}
