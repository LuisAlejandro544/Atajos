package com.example.executor.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.executor.ExecutionResult

class NavigationActionHandler(private val context: Context) {

    fun openApp(packageNameParam: String): ExecutionResult {
        val pkg = packageNameParam.trim()
        if (pkg.isEmpty()) {
            return ExecutionResult(false, "No se seleccionó ninguna aplicación")
        }
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(pkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent != null) {
                context.startActivity(intent)
                val label = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg
                }
                ExecutionResult(true, "Abriendo $label")
            } else {
                ExecutionResult(false, "App no disponible o desinstalada: $pkg")
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Error al abrir app: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    fun openUrl(urlParam: String): ExecutionResult {
        var url = urlParam.trim()
        if (url.isEmpty()) {
            url = "https://www.google.com"
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult(true, "Abriendo $url")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al abrir navegador: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    fun openMapNavigation(destination: String): ExecutionResult {
        val query = destination.trim().ifEmpty { "Casa" }
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Ruta hacia: $query")
        } catch (_: Exception) {
            try {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                ExecutionResult(true, "Buscando en mapas: $query")
            } catch (e: Exception) {
                ExecutionResult(false, "No se pudo abrir mapas: ${e.localizedMessage ?: "Fallo"}")
            }
        }
    }
}
