package com.concatstudio.onegym.media

import com.concatstudio.onegym.model.UploadedImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.ImageOutputStream
import com.typesafe.config.ConfigFactory

data class MediaStorageSettings(
    val rootDirectory: Path,
    val publicPath: String,
    val maxUploadBytes: Long,
    val maxImageDimension: Int,
    val maxImagePixels: Long,
    val jpegQuality: Float
) {
    companion object {
        fun load(): MediaStorageSettings {
            val config = ConfigFactory.load().getConfig("media")
            val root = Paths.get(config.getString("root-directory")).toAbsolutePath().normalize()
            return MediaStorageSettings(
                rootDirectory = root,
                publicPath = config.getString("public-path"),
                maxUploadBytes = config.getLong("max-upload-bytes"),
                maxImageDimension = config.getInt("max-image-dimension"),
                maxImagePixels = config.getLong("max-image-pixels"),
                jpegQuality = config.getDouble("jpeg-quality").toFloat()
            ).also { settings ->
                require(settings.publicPath.startsWith('/')) { "media.public-path must start with /" }
                require(settings.maxUploadBytes > 0) { "media.max-upload-bytes must be positive" }
                require(settings.maxImageDimension > 0) { "media.max-image-dimension must be positive" }
                require(settings.maxImagePixels > 0) { "media.max-image-pixels must be positive" }
                require(settings.jpegQuality in 0.0f..1.0f) { "media.jpeg-quality must be between 0 and 1" }
            }
        }
    }
}

/** Stores optimized image files locally. Database entities persist only the returned URL. */
class LocalImageStorage(private val settings: MediaStorageSettings) {
    suspend fun storeGymImage(gymId: Long, bytes: ByteArray): UploadedImageResponse = withContext(Dispatchers.IO) {
        require(gymId > 0) { "A valid gym ID is required" }
        require(bytes.isNotEmpty()) { "The image is empty" }
        require(bytes.size.toLong() <= settings.maxUploadBytes) { "The image exceeds the maximum upload size" }

        val original = decode(bytes)
        val optimized = resize(original)
        val hasAlpha = optimized.colorModel.hasAlpha() && optimized.transparency != Transparency.OPAQUE
        val format = if (hasAlpha) "png" else "jpeg"
        val extension = if (hasAlpha) "png" else "jpg"
        val contentType = if (hasAlpha) "image/png" else "image/jpeg"
        val fileName = "${UUID.randomUUID()}.$extension"
        val relativeDirectory = "images/gym-$gymId"
        val targetDirectory = settings.rootDirectory.resolve(relativeDirectory).normalize()
        require(targetDirectory.startsWith(settings.rootDirectory)) { "Invalid storage directory" }
        Files.createDirectories(targetDirectory)
        val target = targetDirectory.resolve(fileName).normalize()
        require(target.startsWith(targetDirectory)) { "Invalid file name" }

        write(optimized, format, target)
        UploadedImageResponse(
            url = "${settings.publicPath.trimEnd('/')}/$relativeDirectory/$fileName",
            fileName = fileName,
            contentType = contentType,
            width = optimized.width,
            height = optimized.height,
            sizeBytes = Files.size(target)
        )
    }

    private fun decode(bytes: ByteArray): BufferedImage {
        val input = ImageIO.createImageInputStream(ByteArrayInputStream(bytes))
            ?: throw IllegalArgumentException("Invalid image data")
        input.use { stream ->
            val reader = ImageIO.getImageReaders(stream).asSequence().firstOrNull()
                ?: throw IllegalArgumentException("Only JPEG and PNG images are supported")
            try {
                reader.input = stream
                val format = reader.formatName.lowercase()
                require(format == "jpeg" || format == "jpg" || format == "png") {
                    "Only JPEG and PNG images are supported"
                }
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                require(width > 0 && height > 0) { "Invalid image dimensions" }
                require(width.toLong() * height.toLong() <= settings.maxImagePixels) {
                    "The image dimensions exceed the allowed pixel limit"
                }
                return reader.read(0)
            } finally {
                reader.dispose()
            }
        }
    }

    private fun resize(source: BufferedImage): BufferedImage {
        val largestSide = maxOf(source.width, source.height)
        if (largestSide <= settings.maxImageDimension) return source
        val scale = settings.maxImageDimension.toDouble() / largestSide.toDouble()
        val width = (source.width * scale).toInt().coerceAtLeast(1)
        val height = (source.height * scale).toInt().coerceAtLeast(1)
        val type = if (source.colorModel.hasAlpha()) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        return BufferedImage(width, height, type).also { target ->
            val graphics: Graphics2D = target.createGraphics()
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                graphics.drawImage(source, 0, 0, width, height, null)
            } finally {
                graphics.dispose()
            }
        }
    }

    private fun write(image: BufferedImage, format: String, target: Path) {
        val writer = ImageIO.getImageWritersByFormatName(format).asSequence().firstOrNull()
            ?: throw IllegalStateException("No writer available for $format")
        try {
            ImageIO.createImageOutputStream(Files.newOutputStream(target)).use { output: ImageOutputStream ->
                writer.output = output
                val parameters = writer.defaultWriteParam
                if (format == "jpeg" && parameters.canWriteCompressed()) {
                    parameters.compressionMode = ImageWriteParam.MODE_EXPLICIT
                    parameters.compressionQuality = settings.jpegQuality
                }
                writer.write(null, IIOImage(image, null, null), parameters)
            }
        } catch (error: Throwable) {
            Files.deleteIfExists(target)
            throw error
        } finally {
            writer.dispose()
        }
    }
}
