package com.concatstudio.onegym

import com.concatstudio.onegym.routes.healthRouting
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.server.routing.routing
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

    @Test
    fun `readiness returns 200 when PostgreSQL is reachable`() = testApplication {
        application {
            configureSerialization()
            routing { healthRouting { true } }
        }

        val response = client.get("/health/ready")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"ready"}""", response.bodyAsText())
    }

    @Test
    fun `readiness returns a generic 503 when PostgreSQL is unavailable`() = testApplication {
        application {
            configureSerialization()
            routing { healthRouting { false } }
        }

        val response = client.get("/health/ready")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals("""{"code":"service_unavailable","message":"Service temporarily unavailable"}""", response.bodyAsText())
    }

    @Test
    fun `cors accepts an explicit browser origin and rejects paths and wildcards`() {
        val origin = parseCorsOrigin("https://dashboard.example.com:8443")
        assertEquals("dashboard.example.com:8443", origin.rawAuthority)
        assertFailsWith<IllegalArgumentException> { parseCorsOrigin("https://dashboard.example.com/app") }
        assertFailsWith<IllegalArgumentException> { parseCorsOrigin("https://*.example.com") }
    }

}
