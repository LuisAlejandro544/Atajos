package com.example.engine.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.coroutineContext

/**
 * Gestor de descarga e instalación in-app del paquete APK de Flurix.
 * Mide en tiempo real la velocidad de transferencia, progreso porcentual y peso descargado.
 */
object UpdateDownloadManager {

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (DownloadStatus) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val updatesDir = File(context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(updatesDir, "flurix_update.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            var currentUrl = apkUrl
            var redirectCount = 0
            var finalConnection: HttpURLConnection? = null

            // Manejar posibles redirecciones HTTP (301/302 de GitHub CDN)
            while (redirectCount < 5) {
                val url = URL(currentUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 20000
                    setRequestProperty("User-Agent", "Flurix-App-Android")
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER
                ) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location != null) {
                        currentUrl = location
                        redirectCount++
                        continue
                    }
                }

                finalConnection = conn
                break
            }

            connection = finalConnection ?: throw IllegalStateException("No se pudo establecer conexión con el servidor.")

            val contentLength = connection.contentLength.toLong()
            inputStream = connection.inputStream
            outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(8 * 1024)
            var totalBytesRead = 0L
            var lastUpdateTime = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L
            var currentSpeedFormatted = "0 KB/s"

            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (!coroutineContext.isActive) {
                    destinationFile.delete()
                    return@withContext null
                }

                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                bytesSinceLastUpdate += bytesRead

                val now = System.currentTimeMillis()
                val elapsed = now - lastUpdateTime

                // Actualizar métricas cada 250ms
                if (elapsed >= 250 || (contentLength > 0 && totalBytesRead == contentLength)) {
                    val speedBytesPerSec = if (elapsed > 0) (bytesSinceLastUpdate * 1000) / elapsed else 0L
                    currentSpeedFormatted = formatSpeed(speedBytesPerSec)

                    val progress = if (contentLength > 0) {
                        (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    val downloadedFormatted = formatBytes(totalBytesRead)
                    val totalFormatted = if (contentLength > 0) formatBytes(contentLength) else "..."

                    onProgress(
                        DownloadStatus.Downloading(
                            bytesDownloaded = totalBytesRead,
                            totalBytes = contentLength,
                            progress = progress,
                            speedFormatted = currentSpeedFormatted,
                            downloadedFormatted = downloadedFormatted,
                            totalFormatted = totalFormatted
                        )
                    )

                    lastUpdateTime = now
                    bytesSinceLastUpdate = 0L
                }
            }

            outputStream.flush()

            onProgress(DownloadStatus.Completed(destinationFile))
            destinationFile
        } catch (e: Exception) {
            onProgress(DownloadStatus.Error("Error en la descarga: ${e.localizedMessage ?: e.message}"))
            null
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { inputStream?.close() } catch (_: Exception) {}
            connection?.disconnect()
        }
    }

    /**
     * Lanza el instalador nativo de paquetes de Android utilizando FileProvider.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "El archivo de actualización no existe.", Toast.LENGTH_SHORT).show()
                return
            }

            // En Android 8.0 (API 26) o superior, verificar permiso para instalar paquetes desconocidos
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    Toast.makeText(context, "Permite a Flurix instalar actualizaciones para continuar.", Toast.LENGTH_LONG).show()
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo iniciar la instalación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format(Locale.US, "%d KB/s", bytesPerSec / 1024)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
