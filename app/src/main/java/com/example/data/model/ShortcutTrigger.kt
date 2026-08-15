package com.example.data.model

/**
 * Tipos de disparadores automáticos nativos disponibles para un atajo.
 */
enum class ShortcutTrigger(
    val key: String,
    val label: String,
    val description: String,
    val iconKey: String
) {
    NONE(
        key = "NONE",
        label = "Solo Manual",
        description = "Se ejecuta al tocarlo en la app o desde el icono de la pantalla de inicio",
        iconKey = "play_circle"
    ),
    TIME_EXACT(
        key = "TIME_EXACT",
        label = "A una hora exacta del día",
        description = "Se ejecuta automáticamente a la hora programada todos los días",
        iconKey = "schedule"
    ),
    BATTERY_LEVEL(
        key = "BATTERY_LEVEL",
        label = "Nivel de batería personalizado",
        description = "Se ejecuta automáticamente cuando la batería alcance un porcentaje específico",
        iconKey = "battery_charging_full"
    ),
    POWER_CONNECTED(
        key = "POWER_CONNECTED",
        label = "Al conectar cargador",
        description = "Se ejecuta en segundo plano cuando el teléfono comienza a cargar corriente",
        iconKey = "battery_charging_full"
    ),
    POWER_DISCONNECTED(
        key = "POWER_DISCONNECTED",
        label = "Al desconectar cargador",
        description = "Se ejecuta en segundo plano cuando se desenchufa el teléfono",
        iconKey = "power_off"
    ),
    POWER_BOTH(
        key = "POWER_BOTH",
        label = "Al conectar o desconectar",
        description = "Se activa ante cualquier cambio en el estado de alimentación",
        iconKey = "bolt"
    ),
    BATTERY_LOW(
        key = "BATTERY_LOW",
        label = "Batería baja (<15%)",
        description = "Se ejecuta automáticamente cuando la batería desciende al nivel crítico",
        iconKey = "battery_alert"
    ),
    BATTERY_OK(
        key = "BATTERY_OK",
        label = "Batería restablecida (>20%)",
        description = "Se ejecuta al recuperar un nivel de batería seguro",
        iconKey = "battery_saver"
    ),
    BATTERY_FULL(
        key = "BATTERY_FULL",
        label = "Batería cargada al 100%",
        description = "Se ejecuta cuando el dispositivo finaliza la recarga completa",
        iconKey = "battery_full"
    );

    companion object {
        val BATTERY_EXACT = BATTERY_LEVEL

        fun fromKey(rawKey: String): ShortcutTrigger {
            val baseKey = rawKey.substringBefore(":")
            if (baseKey.equals("BATTERY_EXACT", ignoreCase = true)) return BATTERY_LEVEL
            return entries.firstOrNull { it.key.equals(baseKey, ignoreCase = true) } ?: NONE
        }

        fun extractValue(rawKey: String, defaultValue: String): String {
            return if (rawKey.contains(":")) {
                rawKey.substringAfter(":")
            } else {
                defaultValue
            }
        }

        fun buildBatteryExactKey(level: Int): String = "BATTERY_LEVEL:$level"

        fun getBatteryExactLevel(key: String): Int = extractValue(key, "80").toIntOrNull() ?: 80

        fun buildTimeExactKey(time: String): String = "TIME_EXACT:$time"

        fun getTimeExactValue(key: String): String = extractValue(key, "08:00")
    }
}

