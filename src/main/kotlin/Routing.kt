package com.concatstudio.onegym

import com.concatstudio.onegym.routes.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.logger.SLF4JLogger
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        authenticationRouting()
        authenticate("auth-jwt") {
            profileRouting()
            statusRouting()
            gymNewsRouting()
            gymHomeRouting()
            activePlanRouting()
            coachTypeRouting()
            gymCoachRouting()
            userRouting()
            gymRouting()
            userRoleRouting()
            userGymRouting()
            newsRouting()
            planTypeRouting()
            planRouting()
            weekRouting()
            dayRouting()
            exerciseRouting()
            quantityTypeRouting()
            blockRouting()
            blockExerciseRouting()
            userPlanRouting()
            currentPlanActivityRouting()
            registerRouting()
            userTestRouting()
        }
    }
}
