package com.concatstudio.onegym

import com.concatstudio.onegym.modules.appDiModules
import io.ktor.server.application.Application
import io.ktor.server.application.install
import com.concatstudio.onegym.security.JwtService
import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject

import org.koin.ktor.plugin.Koin
import java.net.URI

fun Application.rootModule() {
    val jwtService: JwtService by inject()
    val corsOrigin = ConfigFactory.load().getString("cors.allowed-origin")
    val allowedOrigin = corsOrigin.takeIf { it.isNotEmpty() }?.let(::parseCorsOrigin)

    install(CORS) {
        allowedOrigin?.let { allowHost(it.rawAuthority, schemes = listOf(it.scheme)) }
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(Koin) {
        configureSerialization()
        modules(appDiModules)

    }
    configureStatusPages()

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

internal fun parseCorsOrigin(value: String): URI {
    val origin = URI(value)
    require(
        origin.scheme in listOf("http", "https") && origin.host != null &&
            origin.rawUserInfo == null && origin.rawPath.isEmpty() &&
            origin.rawQuery == null && origin.rawFragment == null && origin.port in -1..65535
    ) { "The CORS origin must be an HTTP(S) origin with an explicit host and no path" }
    return origin
}
