package com.concatstudio.onegym.routes

import io.ktor.server.application.ApplicationCall

data class PageRequest(val limit: Int, val offset: Long)

fun ApplicationCall.pageRequest(): PageRequest? {
    val rawLimit = request.queryParameters["limit"]
    val rawOffset = request.queryParameters["offset"]
    val limit = rawLimit?.toIntOrNull() ?: if (rawLimit == null) 20 else return null
    val offset = rawOffset?.toLongOrNull() ?: if (rawOffset == null) 0L else return null
    return if (limit in 1..100 && offset in 0..Int.MAX_VALUE.toLong()) PageRequest(limit, offset) else null
}
