package com.concatstudio.onegym.security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal

fun ApplicationCall.authenticatedUserId(): Long? =
    principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()

fun ApplicationCall.isSuperAdmin(authorizationService: AuthorizationService): Boolean =
    authenticatedUserId()?.let(authorizationService::isSuperAdmin) == true
