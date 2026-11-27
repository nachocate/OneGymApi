package com.concatstudio.onegym

import com.concatstudio.onegym.media.LocalImageStorage
import com.concatstudio.onegym.media.MediaStorageSettings
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalImageStorageTest {
    @Test
    fun `stores an optimized image and returns a relative public URL`() = runBlocking {
        val directory = Files.createTempDirectory("onegym-image-test")
        try {
            val storage = LocalImageStorage(settings(directory))
            val response = storage.storeGymImage(7, imageBytes(width = 2_000, height = 1_000))

            assertEquals(1_920, response.width)
            assertEquals(960, response.height)
            assertEquals("image/jpeg", response.contentType)
            assertTrue(response.url.startsWith("/media/images/gym-7/"))
            assertTrue(Files.exists(directory.resolve("images/gym-7/${response.fileName}")))
            assertTrue(response.sizeBytes > 0)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects unsupported image bytes`() {
        runBlocking {
            val directory = Files.createTempDirectory("onegym-image-test")
            try {
                val storage = LocalImageStorage(settings(directory))
                assertFailsWith<IllegalArgumentException> {
                    storage.storeGymImage(7, "not an image".encodeToByteArray())
                }
            } finally {
                directory.toFile().deleteRecursively()
            }
        }
    }

    private fun settings(root: java.nio.file.Path) = MediaStorageSettings(
        rootDirectory = root,
        publicPath = "/media",
        maxUploadBytes = 5 * 1024 * 1024,
        maxImageDimension = 1_920,
        maxImagePixels = 16_000_000,
        jpegQuality = 0.82f
    )

    private fun imageBytes(width: Int, height: Int): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = Color(34, 139, 34)
            graphics.fillRect(0, 0, width, height)
        } finally {
            graphics.dispose()
        }
        return ByteArrayOutputStream().use { output ->
            check(ImageIO.write(image, "png", output))
            output.toByteArray()
        }
    }
}
