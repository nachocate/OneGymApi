package com.concatstudio.onegym

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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

    @Test
    fun `cors preflight permits dashboard PUT requests`() = testApplication {
        application {
            rootModule()
        }

        val response = client.options("/gyms/1/plans/21") {
            header(HttpHeaders.Origin, "http://localhost:5173")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Put.value)
            header(HttpHeaders.AccessControlRequestHeaders, "authorization,content-type")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

}
