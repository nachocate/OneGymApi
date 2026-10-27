package com.concatstudio.onegym.model

import kotlinx.serialization.Serializable

@Serializable
data class UploadedImageResponse(
    /** Relative public URL. Persist this value in imageUrl, logoUrl or bannerUrl. */
    val url: String,
    val fileName: String,
    val contentType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long
)
