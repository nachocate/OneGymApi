package com.concatstudio.onegym

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    init {
        System.setProperty("JWT_SECRET", "test-access-secret-that-is-at-least-32-characters")
        System.setProperty("JWT_REFRESH_TOKEN_HASH_SECRET", "test-refresh-secret-that-is-at-least-32-characters")
    }

    @Test
    fun `test root endpoint`() = testApplication {
        application {
            rootModule()
        }
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

}
