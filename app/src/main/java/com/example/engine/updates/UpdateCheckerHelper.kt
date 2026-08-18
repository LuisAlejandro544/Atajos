package com.example.engine.updates

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Consultor asíncrono para verificar nuevas versiones de Flurix en GitHub Releases.
 * Filtra automáticamente según el canal instalado (Beta, Dev, Estable o Canary) y compara versiones.
 */
object UpdateCheckerHelper {

    private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/LuisAlejandro544/Flurix/releases"
    private const val FALLBACK_RELEASES_URL = "https://api.github.com/repos/LuisAlejandro544/Atajos/releases"

    suspend fun checkForUpdates(
        currentVersionName: String,
        currentChannel: String
    ): UpdateCheckStatus = withContext(Dispatchers.IO) {
        try {
            var jsonString = fetchReleasesJson(GITHUB_RELEASES_URL)
            if (jsonString.isNullOrBlank() || jsonString.trim().startsWith("{\"message\"")) {
                jsonString = fetchReleasesJson(FALLBACK_RELEASES_URL)
            }

            if (jsonString.isNullOrBlank() || !jsonString.trim().startsWith("[")) {
                return@withContext UpdateCheckStatus.Error("No se pudieron obtener los releases del repositorio.")
            }

            val releasesArray = JSONArray(jsonString)
            if (releasesArray.length() == 0) {
                return@withContext UpdateCheckStatus.UpToDate
            }

            val allReleases = mutableListOf<AppReleaseInfo>()

            for (i in 0 until releasesArray.length()) {
                val releaseObj = releasesArray.getJSONObject(i)
                val tagName = releaseObj.optString("tag_name", "").trim()
                val name = releaseObj.optString("name", tagName)
                val changelog = releaseObj.optString("body", "Sin notas de versión disponibles.")
                val htmlUrl = releaseObj.optString("html_url", "https://github.com/LuisAlejandro544/Flurix/releases")
                val isPrerelease = releaseObj.optBoolean("prerelease", false)
                val publishedAt = releaseObj.optString("published_at", "")

                // Extraer el archivo .apk de los assets
                var apkDownloadUrl = ""
                var apkSize = 0L

                val assetsArray = releaseObj.optJSONArray("assets")
                if (assetsArray != null) {
                    for (j in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(j)
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", "")
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                // Identificar a qué canal pertenece este release
                val releaseChannel = detectReleaseChannel(tagName, name, isPrerelease)
                val isNewer = isVersionNewer(remoteVersionTag = tagName, localVersionName = currentVersionName)

                allReleases.add(
                    AppReleaseInfo(
                        tagName = tagName,
                        name = name,
                        changelog = changelog,
                        apkDownloadUrl = apkDownloadUrl,
                        apkSizeInBytes = apkSize,
                        htmlUrl = htmlUrl,
                        isPrerelease = isPrerelease,
                        publishedAt = publishedAt,
                        channel = releaseChannel,
                        isNewerThanCurrent = isNewer
                    )
                )
            }

            // Filtrar según el canal del usuario
            val matchingReleases = allReleases.filter { release ->
                isCompatibleWithChannel(release, currentChannel)
            }

            // Seleccionar el release más relevante (el primero coincidente o el más reciente global si no hay específicos)
            val selectedRelease = matchingReleases.firstOrNull() ?: allReleases.firstOrNull()

            if (selectedRelease == null) {
                return@withContext UpdateCheckStatus.UpToDate
            }

            if (selectedRelease.isNewerThanCurrent) {
                UpdateCheckStatus.UpdateAvailable(selectedRelease)
            } else {
                UpdateCheckStatus.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckStatus.Error("Error al consultar actualizaciones: ${e.localizedMessage ?: e.message}")
        }
    }

    private fun detectReleaseChannel(tagName: String, name: String, isPrerelease: Boolean): String {
        val combined = "$tagName $name".uppercase(Locale.ROOT)
        return when {
            combined.contains("-BETA") || combined.contains("-B") -> "BETA"
            combined.contains("-DEV") || combined.contains("-D") -> "DEV"
            combined.contains("-CANARY") || combined.contains("-CREATOR") -> "CANARY"
            combined.contains("-E") || !isPrerelease -> "STABLE"
            else -> "BETA"
        }
    }

    private fun isCompatibleWithChannel(release: AppReleaseInfo, currentChannel: String): Boolean {
        val upperCurrent = currentChannel.uppercase(Locale.ROOT)
        val upperReleaseChannel = release.channel.uppercase(Locale.ROOT)
        return when (upperCurrent) {
            "BETA" -> upperReleaseChannel == "BETA" || release.tagName.contains("-B", ignoreCase = true)
            "DEV" -> upperReleaseChannel == "DEV" || release.tagName.contains("-DEV", ignoreCase = true) || release.tagName.contains("-D", ignoreCase = true)
            "CANARY" -> upperReleaseChannel == "CANARY" || release.tagName.contains("-CANARY", ignoreCase = true)
            else -> upperReleaseChannel == "STABLE" || (!release.isPrerelease && !release.tagName.contains("-B") && !release.tagName.contains("-DEV"))
        }
    }

    /**
     * Compara semánticamente la versión remota con la local.
     */
    fun isVersionNewer(remoteVersionTag: String, localVersionName: String): Boolean {
        val remoteClean = cleanVersionString(remoteVersionTag)
        val localClean = cleanVersionString(localVersionName)

        val remoteParts = remoteClean.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = localClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }

        // Si los números son iguales, revisar sufijos o tags
        return false
    }

    private fun cleanVersionString(version: String): String {
        return version
            .removePrefix("v")
            .removePrefix("V")
            .split("-")
            .firstOrNull() ?: version
    }

    private fun fetchReleasesJson(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Flurix-App-Android")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
