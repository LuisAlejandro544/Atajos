package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TriggerType(val label: String, val description: String, val iconKey: String) {
    TIME_OF_DAY("Hora del día", "Se activa a una hora programada específica", "schedule"),
    CHARGER_CONNECTED("Al conectar cargador", "Se activa cuando el teléfono empieza a cargar", "battery_charging_full"),
    BATTERY_LOW("Batería baja (<20%)", "Se activa cuando el nivel de batería desciende", "battery_alert"),
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
