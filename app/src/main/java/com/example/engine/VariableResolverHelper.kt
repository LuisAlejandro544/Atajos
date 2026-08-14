package com.example.engine

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Descriptor de una variable del sistema disponible para interpolar.
 */
data class SystemVariable(
    val tag: String,
    val label: String,
    val iconName: String,
    val previewValue: String
)

/**
 * Motor para resolver e interpolar variables del sistema ({HORA}, {FECHA}, {BATERIA}, etc.)
 * en cadenas de texto para Voz, Notificaciones y Mensajería.
 */
object VariableResolverHelper {

    val AVAILABLE_VARIABLES = listOf(
        SystemVariable(
            tag = "{HORA}",
            label = "Hora actual",
            iconName = "schedule",
            previewValue = "14:30"
        ),
        SystemVariable(
            tag = "{FECHA}",
            label = "Fecha de hoy",
            iconName = "calendar_today",
            previewValue = "14 ago"
        ),
        SystemVariable(
            tag = "{DIA_SEMANA}",
            label = "Día de la semana",
            iconName = "event",
            previewValue = "Viernes"
        ),
        SystemVariable(
            tag = "{BATERIA}",
            label = "Nivel de batería",
            iconName = "battery_charging_full",
            previewValue = "85%"
        ),
        SystemVariable(
            tag = "{ESTADO_BATERIA}",
            label = "Estado de carga",
            iconName = "power",
            previewValue = "Cargando"
        ),
        SystemVariable(
            tag = "{PORTAPAPELES}",
            label = "Texto copiado",
            iconName = "content_paste",
            previewValue = "Texto copiado"
        ),
        SystemVariable(
            tag = "{DISPOSITIVO}",
            label = "Modelo de teléfono",
            iconName = "smartphone",
            previewValue = "Android"
        )
    )

    /**
     * Resuelve y reemplaza todas las etiquetas encontradas en el texto por sus valores reales del sistema.
     */
    fun resolve(text: String, context: Context): String {
        if (text.isBlank()) return text

        var resolved = text

        // 1. Hora ({HORA})
        if (resolved.contains("{HORA}", ignoreCase = true)) {
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            resolved = resolved.replace("(?i)\\{HORA\\}".toRegex(), timeStr)
        }

        // 2. Fecha ({FECHA})
        if (resolved.contains("{FECHA}", ignoreCase = true)) {
            val dateStr = SimpleDateFormat("d 'de' MMMM", Locale.getDefault()).format(Date())
            resolved = resolved.replace("(?i)\\{FECHA\\}".toRegex(), dateStr)
        }

        // 3. Día de la semana ({DIA_SEMANA})
        if (resolved.contains("{DIA_SEMANA}", ignoreCase = true)) {
            val dayStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            resolved = resolved.replace("(?i)\\{DIA_SEMANA\\}".toRegex(), dayStr)
        }

        // 4. Batería ({BATERIA} y {ESTADO_BATERIA})
        if (resolved.contains("{BATERIA}", ignoreCase = true) || resolved.contains("{ESTADO_BATERIA}", ignoreCase = true)) {
            val (batteryLevel, isCharging) = getBatteryInfo(context)
            resolved = resolved.replace("(?i)\\{BATERIA\\}".toRegex(), "$batteryLevel%")
            val chargingText = if (isCharging) "Cargando" else "Desconectado"
            resolved = resolved.replace("(?i)\\{ESTADO_BATERIA\\}".toRegex(), chargingText)
        }

        // 5. Portapapeles ({PORTAPAPELES})
        if (resolved.contains("{PORTAPAPELES}", ignoreCase = true)) {
            val clipboardText = getClipboardText(context)
            resolved = resolved.replace("(?i)\\{PORTAPAPELES\\}".toRegex(), clipboardText)
        }

        // 6. Dispositivo ({DISPOSITIVO})
        if (resolved.contains("{DISPOSITIVO}", ignoreCase = true)) {
            val deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
            resolved = resolved.replace("(?i)\\{DISPOSITIVO\\}".toRegex(), deviceName)
        }

        return resolved
    }

    private fun getBatteryInfo(context: Context): Pair<Int, Boolean> {
        return try {
            val batteryStatus: Intent? = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val batteryPct = if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                50
            }

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            Pair(batteryPct, isCharging)
        } catch (e: Exception) {
            Pair(50, false)
        }
    }

    private fun getClipboardText(context: Context): String {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val item = clipboard?.primaryClip?.getItemAt(0)
            val text = item?.text?.toString()
            if (!text.isNullOrBlank()) text else "nada en el portapapeles"
        } catch (e: Exception) {
            "nada en el portapapeles"
        }
    }
}
