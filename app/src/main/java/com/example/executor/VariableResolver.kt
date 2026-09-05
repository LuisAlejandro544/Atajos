package com.example.executor

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VariableResolver {

    data class VariableInfo(
        val key: String,
        val label: String,
        val example: String
    )

    val AVAILABLE_VARIABLES = listOf(
        VariableInfo("{hora}", "Hora", "16:45"),
        VariableInfo("{hora_segundos}", "Hora exacta", "16:45:30"),
        VariableInfo("{fecha}", "Fecha", "05/09/2026"),
        VariableInfo("{dia}", "Día de la semana", "Sábado"),
        VariableInfo("{bateria}", "Batería %", "82"),
        VariableInfo("{portapapeles}", "Portapapeles", "Texto copiado")
    )

    fun getBatteryPercentage(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (capacity != null && capacity > 0 && capacity <= 100) {
                capacity
            } else {
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, filter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
                } else 100
            }
        } catch (_: Exception) {
            100
        }
    }

    fun getClipboardContent(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = cm?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0)?.coerceToText(context)?.toString()?.trim() ?: ""
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Resuelve etiquetas como {hora}, {fecha}, {bateria}, {portapapeles} en tiempo real.
     */
    fun resolve(text: String, context: Context): String {
        if (!text.contains("{")) return text

        val now = Date()
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val timeWithSeconds = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
        val day = SimpleDateFormat("EEEE", Locale("es", "ES")).format(now).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        val battery = getBatteryPercentage(context).toString()
        val clipboard = getClipboardContent(context).ifBlank { "(portapapeles vacío)" }

        return text
            .replace("{hora}", time, ignoreCase = true)
            .replace("{hora_segundos}", timeWithSeconds, ignoreCase = true)
            .replace("{fecha}", date, ignoreCase = true)
            .replace("{dia}", day, ignoreCase = true)
            .replace("{día}", day, ignoreCase = true)
            .replace("{bateria}", battery, ignoreCase = true)
            .replace("{batería}", battery, ignoreCase = true)
            .replace("{portapapeles}", clipboard, ignoreCase = true)
    }
}
