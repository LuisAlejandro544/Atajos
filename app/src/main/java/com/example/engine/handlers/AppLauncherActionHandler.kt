package com.example.engine.handlers

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

/**
 * Manejador especializado en abrir juegos o aplicaciones instaladas mediante el PackageManager.
 */
class AppLauncherActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.LAUNCH_APP)

    override suspend fun execute(action: ShortcutAction): String {
        val packageName = action.param1.trim()
        val appName = action.param2.ifBlank { packageName }

        if (packageName.isBlank()) {
            return "No se ha configurado ninguna app o juego para abrir"
        }

        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                "Abriendo $appName"
            } else {
                Toast.makeText(context, "No se pudo abrir $appName (¿está instalada?)", Toast.LENGTH_LONG).show()
                "Aplicación no encontrada: $packageName"
            }
        } catch (e: Exception) {
            "Error al abrir $appName: ${e.message}"
        }
    }
}
