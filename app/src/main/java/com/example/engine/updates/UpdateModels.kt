package com.example.engine.updates

import java.io.File

/**
 * Información estructurada de un release de GitHub.
 */
data class AppReleaseInfo(
    val tagName: String,
    val name: String,
    val changelog: String,
    val apkDownloadUrl: String,
    val apkSizeInBytes: Long,
    val htmlUrl: String,
    val isPrerelease: Boolean,
    val publishedAt: String,
    val channel: String,
    val isNewerThanCurrent: Boolean
) {
    val formattedApkSize: String
        get() {
            if (apkSizeInBytes <= 0) return "Tamaño desconocido"
            val mb = apkSizeInBytes / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.1f MB", mb)
        }
}

/**
 * Estado del proceso de comprobación de actualizaciones.
 */
sealed interface UpdateCheckStatus {
    data object Idle : UpdateCheckStatus
    data object Checking : UpdateCheckStatus
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateCheckStatus
    data object UpToDate : UpdateCheckStatus
    data class Error(val message: String) : UpdateCheckStatus
}

/**
 * Estado de descarga en tiempo real del archivo APK.
 */
sealed interface DownloadStatus {
    data object Idle : DownloadStatus
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progress: Float,
        val speedFormatted: String,
        val downloadedFormatted: String,
        val totalFormatted: String
    ) : DownloadStatus
    data class Completed(val apkFile: File) : DownloadStatus
    data class Error(val message: String) : DownloadStatus
}
