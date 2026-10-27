package com.concatstudio.onegym.routes

import com.concatstudio.onegym.media.LocalImageStorage
import com.concatstudio.onegym.media.MediaStorageSettings
import com.concatstudio.onegym.security.AuthorizationService
import com.concatstudio.onegym.security.authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.readAvailable
import org.koin.ktor.ext.inject
import java.io.ByteArrayOutputStream

/** Uploads optimized local images. Persist only the returned relative URL in PostgreSQL. */
fun Route.imageUploadRouting() {
    val storage: LocalImageStorage by inject()
    val settings: MediaStorageSettings by inject()
    val authorization: AuthorizationService by inject()

    route("/gyms/{gymId}/uploads") {
        post("/images") {
            val gymId = call.parameters["gymId"]?.toLongOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val actorId = call.authenticatedUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (!authorization.canManageTraining(actorId, gymId)) return@post call.respond(HttpStatusCode.Forbidden)

            try {
                var uploaded = false
                var result: com.concatstudio.onegym.model.UploadedImageResponse? = null
                call.receiveMultipart().forEachPart { part ->
                    try {
                        if (part is PartData.FileItem && part.name == "image") {
                            require(!uploaded) { "Send exactly one image field" }
                            uploaded = true
                            val bytes = part.provider().readBytesUpTo(settings.maxUploadBytes)
                            result = storage.storeGymImage(gymId, bytes)
                        }
                    } finally {
                        part.release.invoke()
                    }
                }
                call.respond(HttpStatusCode.Created, result ?: throw IllegalArgumentException("The image field is required"))
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

private suspend fun io.ktor.utils.io.ByteReadChannel.readBytesUpTo(maxBytes: Long): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val read = readAvailable(buffer)
        if (read == -1) break
        total += read
        require(total <= maxBytes) { "The image exceeds the maximum upload size" }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
