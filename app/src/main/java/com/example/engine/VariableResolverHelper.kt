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
            tag = "{ULTIMA_NOTIFICACION}",
            label = "Texto de notificación",
            iconName = "notifications",
            previewValue = "Es la hora de jugar"
        ),
        SystemVariable(
            tag = "{NOTIFICACION_TITULO}",
            label = "Título de notificación",
            iconName = "notifications_active",
            previewValue = "Aviso"
        ),
        SystemVariable(
            tag = "{RESPUESTA_WEB}",
            label = "Respuesta de petición web / API",
            iconName = "http",
            previewValue = "{\"status\": \"ok\"}"
        ),
        SystemVariable(
            tag = "{HTTP_STATUS}",
            label = "Código de estado HTTP",
            iconName = "language",
            previewValue = "200"
        ),
        SystemVariable(
            tag = "{DISPOSITIVO}",
            label = "Modelo de teléfono",
            iconName = "smartphone",
            previewValue = "Android"
        )
    )

    /**
     * Resuelve y reemplaza todas las etiquetas encontradas en el texto por sus valores reales del sistema
     * o valores contextuales de la ejecución (notificaciones previas, etc.).
     */
    fun resolve(
        text: String,
        context: Context,
        lastNotificationText: String? = null,
        lastNotificationTitle: String? = null
    ): String {
        if (text.isBlank()) return text

        var resolved = text

        // 0. Última Notificación ({ULTIMA_NOTIFICACION} y {NOTIFICACION_TITULO})
        if (resolved.contains("{ULTIMA_NOTIFICACION}", ignoreCase = true) ||
            resolved.contains("{NOTIFICACION}", ignoreCase = true) ||
            resolved.contains("{NOTIFICACION_TEXTO}", ignoreCase = true)
        ) {
            val notifText = lastNotificationText?.ifBlank { null }
                ?: getCachedNotification(context)
            resolved = resolved
                .replace("(?i)\\{ULTIMA_NOTIFICACION\\}".toRegex(), notifText)
                .replace("(?i)\\{NOTIFICACION\\}".toRegex(), notifText)
                .replace("(?i)\\{NOTIFICACION_TEXTO\\}".toRegex(), notifText)
        }

        if (resolved.contains("{NOTIFICACION_TITULO}", ignoreCase = true)) {
            val notifTitle = lastNotificationTitle?.ifBlank { null }
                ?: getCachedNotificationTitle(context)
            resolved = resolved.replace("(?i)\\{NOTIFICACION_TITULO\\}".toRegex(), notifTitle)
        }

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

        // 6. Respuesta Web / Webhook ({RESPUESTA_WEB}, {ULTIMA_RESPUESTA_WEB}, {HTTP_STATUS})
        if (resolved.contains("{RESPUESTA_WEB}", ignoreCase = true) ||
            resolved.contains("{ULTIMA_RESPUESTA_WEB}", ignoreCase = true) ||
            resolved.contains("{RESPUESTA_HTTP}", ignoreCase = true)
        ) {
            val webResponse = getCachedWebResponse(context)
            resolved = resolved
                .replace("(?i)\\{RESPUESTA_WEB\\}".toRegex(), webResponse)
                .replace("(?i)\\{ULTIMA_RESPUESTA_WEB\\}".toRegex(), webResponse)
                .replace("(?i)\\{RESPUESTA_HTTP\\}".toRegex(), webResponse)
        }

        if (resolved.contains("{HTTP_STATUS}", ignoreCase = true) ||
            resolved.contains("{ESTADO_HTTP}", ignoreCase = true)
        ) {
            val httpStatus = getCachedHttpStatus(context)
            resolved = resolved
                .replace("(?i)\\{HTTP_STATUS\\}".toRegex(), httpStatus)
                .replace("(?i)\\{ESTADO_HTTP\\}".toRegex(), httpStatus)
        }

        // 7. Dispositivo ({DISPOSITIVO})
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

    private var cachedLastNotificationBody: String = "Es la hora de jugar"
    private var cachedLastNotificationTitle: String = "Aviso de Atajos"
    private var cachedLastWebResponse: String = "{\"status\": \"success\"}"
    private var cachedLastHttpStatus: String = "200"

    fun saveLastNotification(title: String, body: String) {
        if (title.isNotBlank()) cachedLastNotificationTitle = title
        if (body.isNotBlank()) cachedLastNotificationBody = body
    }

    fun getCachedNotification(context: Context): String = cachedLastNotificationBody

    fun getCachedNotificationTitle(context: Context): String = cachedLastNotificationTitle

    fun saveLastWebResponse(response: String, status: String = "200") {
        if (response.isNotBlank()) cachedLastWebResponse = response
        if (status.isNotBlank()) cachedLastHttpStatus = status
    }

    fun getCachedWebResponse(context: Context): String = cachedLastWebResponse

    fun getCachedHttpStatus(context: Context): String = cachedLastHttpStatus
}
