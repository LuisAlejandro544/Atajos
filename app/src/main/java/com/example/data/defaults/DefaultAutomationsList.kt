package com.example.data.defaults

import com.example.data.model.AutomationEntity
import com.example.data.model.TriggerType

/**
 * Proveedor modular de las automatizaciones de ejemplo iniciales.
 */
object DefaultAutomationsList {

    fun get(): List<AutomationEntity> {
        return listOf(
            AutomationEntity(
                id = 1L,
                title = "Buenos días al abrir la app",
                triggerType = TriggerType.APP_OPENED,
                triggerValue = "",
                shortcutId = 2L,
                shortcutTitle = "Asistente de Voz: Saludo",
                isEnabled = true,
                notifyWhenRun = true
            ),
            AutomationEntity(
                id = 2L,
                title = "Aviso al conectar cargador",
                triggerType = TriggerType.CHARGER_CONNECTED,
                triggerValue = "Cargando",
                shortcutId = 2L,
                shortcutTitle = "Asistente de Voz: Saludo",
                isEnabled = true,
                notifyWhenRun = true
            )
        )
    }
}
