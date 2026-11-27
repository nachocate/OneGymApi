package com.concatstudio.onegym

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer

fun main(args: Array<String>) {
    embeddedServer(
        factory = io.ktor.server.netty.Netty,
        port = 8080,
        host = ConfigFactory.load().getString("server.host"),
        module = Application::rootModule
    ).start(wait = true)
}
