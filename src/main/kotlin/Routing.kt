package com.concatstudio.onegym

import com.concatstudio.onegym.routes.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.logger.SLF4JLogger
import org.koin.ktor.ext.inject
import com.concatstudio.onegym.media.MediaStorageSettings
import io.ktor.server.http.content.staticFiles

fun Application.configureRouting() {
    val mediaSettings: MediaStorageSettings by inject()
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        authenticationRouting()
        staticFiles(mediaSettings.publicPath, mediaSettings.rootDirectory.toFile())
        authenticate("auth-jwt") {
            profileRouting()
            statusRouting()
            gymNewsRouting()
            gymHomeRouting()
            activePlanRouting()
            coachTypeRouting()
            gymCoachRouting()
            gymCoachDashboardRouting()
            userRouting()
            gymRouting()
            gymMemberRouting()
            gymScopedReadRouting()
            planificationRouting()
            imageUploadRouting()
            gymBusinessHourRouting()
            userRoleRouting()
            userGymRouting()
            newsRouting()
            planTypeRouting()
            planRouting()
            weekRouting()
            dayRouting()
            exerciseRouting()
            evaluationRouting()
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
