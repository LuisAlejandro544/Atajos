package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageCompressor {

    /**
     * Procesa una imagen o icono desde la galería o almacenamiento en un hilo secundario (Dispatchers.IO),
     * redimensionándola a proporciones ideales y comprimiéndola en formato WebP sin pérdida de calidad (Lossless).
     *
     * @param context Contexto de la aplicación
     * @param uri URI del archivo seleccionado (Photo Picker)
     * @param isIcon true si es para icono de atajo (max 256px), false si es fondo de tarjeta (max 1080px)
     * @return Ruta absoluta del archivo .webp generado en almacenamiento interno
     */
    suspend fun compressToWebpLossless(
        context: Context,
        uri: Uri,
        isIcon: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val maxDimension = if (isIcon) 256 else 1080
            val prefix = if (isIcon) "icon" else "card_bg"

            // 1. Obtener dimensiones originales sin cargar el bitmap completo en RAM
            var options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 2. Decodificar bitmap real con el inSampleSize calculado
            options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var bitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))

            // 3. Corregir orientación EXIF si proviene de la cámara del teléfono
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                    if (rotationDegrees != 0f) {
                        val matrix = Matrix().apply { postRotate(rotationDegrees) }
                        val rotated = Bitmap.createBitmap(
                            bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, matrix, true
                        )
                        if (rotated != bitmap) {
                            bitmap?.recycle()
                            bitmap = rotated
                        }
                    }
                }
            } catch (_: Exception) {
                // Si no hay metadatos EXIF, continuamos con el bitmap actual
            }

            // 4. Escalar suavemente al tamaño exacto si excede maxDimension
            val originalWidth = bitmap!!.width
            val originalHeight = bitmap!!.height
            val scale = (maxDimension.toFloat() / maxOf(originalWidth, originalHeight)).coerceAtMost(1.0f)

            val finalBitmap = if (scale < 1.0f) {
                val scaledWidth = (originalWidth * scale).toInt().coerceAtLeast(1)
                val scaledHeight = (originalHeight * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap!!, scaledWidth, scaledHeight, true)
                if (scaled != bitmap) {
                    bitmap?.recycle()
                }
                scaled
            } else {
                bitmap!!
            }

            // 5. Guardar en almacenamiento interno como WebP Lossless
            val mediaDir = File(context.filesDir, "shortcut_media").apply { mkdirs() }
            val outputFile = File(mediaDir, "${prefix}_${System.currentTimeMillis()}.webp")

            FileOutputStream(outputFile).use { outStream ->
                val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                finalBitmap.compress(compressFormat, 100, outStream)
            }

            finalBitmap.recycle()
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un archivo de imagen multimedia si ya no se utiliza
     */
    fun deleteMediaFile(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        } catch (_: Exception) {}
    }
}
