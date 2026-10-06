package com.concatstudio.onegym

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer

fun main(args: Array<String>) {
    embeddedServer(
        factory = io.ktor.server.netty.Netty,
        port = 8080,
        host = "127.0.0.1",
        module = Application::rootModule
    ).start(wait = true)
}
