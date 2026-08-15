package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TriggerType(val label: String, val description: String, val iconKey: String) {
    TIME_OF_DAY("Hora programada", "Se activa a una hora específica del día", "schedule"),
    BATTERY_EXACT("Porcentaje exacto de batería", "Se activa al alcanzar un % exacto de batería", "battery_charging_full"),
    CHARGER_CONNECTED("Al conectar cargador", "Se activa cuando el teléfono empieza a cargar", "battery_charging_full"),
    CHARGER_DISCONNECTED("Al desconectar cargador", "Se activa cuando el cargador se desenchufa", "power_off"),
    CHARGER_BOTH("Al conectar o desconectar", "Se activa al enchufar o desenchufar el cargador", "power"),
    BATTERY_LOW("Batería baja (<20%)", "Se activa cuando el nivel de batería desciende", "battery_alert"),
    BATTERY_OK("Batería restablecida", "Se activa al recuperar nivel normal de batería", "battery_saver"),
    BATTERY_FULL("Batería 100%", "Se activa al completarse la carga al 100%", "battery_full"),
    APP_OPENED("Al abrir la app", "Se activa automáticamente al iniciar Atajos", "play_circle")
}

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val triggerType: TriggerType,
    val triggerValue: String = "", // e.g. "08:30" or "20"
    val shortcutId: Long,
    val shortcutTitle: String = "",
    val isEnabled: Boolean = true,
    val notifyWhenRun: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
