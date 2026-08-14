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
        label = "Manual",
        description = "Se ejecuta únicamente al tocarlo o desde el acceso directo",
        iconKey = "play_circle"
    ),
    POWER_CONNECTED(
        key = "POWER_CONNECTED",
        label = "Al conectar cargador",
        description = "Se activa automáticamente al conectar el teléfono a la corriente",
        iconKey = "battery_charging_full"
    ),
    POWER_DISCONNECTED(
        key = "POWER_DISCONNECTED",
        label = "Al desconectar cargador",
        description = "Se activa automáticamente al desenchufar el cargador",
        iconKey = "power_off"
    ),
    POWER_BOTH(
        key = "POWER_BOTH",
        label = "Al conectar o desconectar",
        description = "Se activa en ambos cambios de estado de alimentación",
        iconKey = "bolt"
    );

    companion object {
        fun fromKey(key: String): ShortcutTrigger {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: NONE
        }
    }
}
