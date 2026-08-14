package com.example.engine.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import kotlin.math.roundToInt

/**
 * Manejador especializado para el control y ajuste del brillo de la pantalla.
 * Soporta incrementos, decrementos, niveles predefinidos (10%, 50%, 100%) y porcentajes exactos.
 */
class BrightnessActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.SET_BRIGHTNESS)

    override suspend fun execute(action: ShortcutAction): String {
        val modeOrValue = action.param1.trim().lowercase().ifBlank { "50" }

        // Si tenemos permiso en Android M+ para modificar ajustes del sistema
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        "Concede el permiso 'Modificar ajustes del sistema' para ajustar el brillo",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return "Permiso 'Modificar ajustes' necesario para controlar el brillo"
            }
        }

        return try {
            val contentResolver = context.contentResolver
            val currentBrightness = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                128
            }

            val currentPct = ((currentBrightness / 255f) * 100).roundToInt().coerceIn(0, 100)

            val targetPct = when (modeOrValue) {
                "increase", "subir", "+20" -> (currentPct + 20).coerceAtMost(100)
                "decrease", "bajar", "-20" -> (currentPct - 20).coerceAtLeast(5)
                "max", "maximo", "100" -> 100
                "min", "minimo", "10" -> 10
                "medium", "medio", "50" -> 50
                else -> {
                    modeOrValue.toIntOrNull()?.coerceIn(0, 100) ?: 50
                }
            }

            val targetBrightnessValue = ((targetPct / 100f) * 255).roundToInt().coerceIn(10, 255)

            // Desactivar brillo automático si está activo para que el cambio tome efecto de inmediato
            try {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            } catch (ignored: Exception) {}

            // Aplicar nuevo valor de brillo
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                targetBrightnessValue
            )

            "Brillo ajustado al $targetPct%"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al cambiar brillo: ${e.localizedMessage ?: "Fallo de permisos"}"
        }
    }
}
