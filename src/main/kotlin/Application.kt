package com.concatstudio.onegym

import com.concatstudio.onegym.modules.appDiModules
import io.ktor.server.application.Application
import io.ktor.server.application.install

import org.koin.ktor.plugin.Koin

fun Application.rootModule() {
    install(Koin) {
        configureSerialization()
        modules(appDiModules)

    }

    configureRouting()
}
