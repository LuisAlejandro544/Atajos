package com.example.data.db

import com.example.data.model.ActionJsonHelper
import com.example.data.model.ActionType
import com.example.data.model.AutomationEntity
import com.example.data.model.ShortcutAction
import com.example.data.model.ShortcutEntity
import com.example.data.model.TriggerType

object DefaultShortcutsProvider {

    fun getDefaultShortcuts(): List<ShortcutEntity> {
        val s1 = ShortcutEntity(
            id = 1L,
            title = "Linterna Rápida & Vibrar",
            description = "Enciende la linterna y confirma con pulso háptico",
            colorHex = "#F59E0B", // Amber
            iconKey = "flashlight_on",
            category = "Utilidades",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.VIBRATE,
                        title = "Vibración de confirmación",
                        param1 = "150",
                        param2 = "single"
                    ),
                    ShortcutAction(
                        type = ActionType.TOGGLE_FLASHLIGHT,
                        title = "Alternar Linterna",
                        param1 = "toggle"
                    ),
                    ShortcutAction(
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Alerta Linterna",
                        param1 = "Linterna",
                        param2 = "Estado de linterna actualizado"
                    )
                )
            ),
            isFavorite = true
        )

        val s2 = ShortcutEntity(
            id = 2L,
            title = "Asistente de Voz: Saludo",
            description = "Te da los buenos días y la bienvenida con síntesis de voz",
            colorHex = "#4F46E5", // Indigo
            iconKey = "record_voice_over",
            category = "Sistema",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.VIBRATE,
                        title = "Vibración suave",
                        param1 = "100"
                    ),
                    ShortcutAction(
                        type = ActionType.SPEAK_TEXT,
                        title = "Pronunciar saludo",
                        param1 = "¡Hola! Bienvenido a tu centro de atajos de Android. Todo listo para automatizar.",
                        param2 = "es"
                    )
                )
            ),
            isFavorite = true
        )

        val s3 = ShortcutEntity(
            id = 3L,
            title = "Búsqueda Rápida en Google",
            description = "Abre el buscador para explorar cualquier duda al instante",
            colorHex = "#2563EB", // Blue
            iconKey = "search",
            category = "Navegación",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.SEARCH_WEB,
                        title = "Buscar novedades",
                        param1 = "noticias tecnológicas de hoy"
                    )
                )
            ),
            isFavorite = false
        )

        val s4 = ShortcutEntity(
            id = 4L,
            title = "Mensaje Rápido 'Voy en camino'",
            description = "Abre WhatsApp con el mensaje listo para enviar",
            colorHex = "#10B981", // Emerald green
            iconKey = "chat",
            category = "Comunicación",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.VIBRATE,
                        title = "Pulso de salida",
                        param1 = "100"
                    ),
                    ShortcutAction(
                        type = ActionType.SEND_WHATSAPP,
                        title = "WhatsApp Rápido",
                        param1 = "",
                        param2 = "¡Hola! Ya voy en camino hacia allí 🚗💨"
                    )
                )
            ),
            isFavorite = true
        )

        val s5 = ShortcutEntity(
            id = 5L,
            title = "Temporizador Enfoque (Pomodoro)",
            description = "Inicia temporizador de 25 min para concentración total",
            colorHex = "#EC4899", // Pink
            iconKey = "timer",
            category = "Productividad",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.SPEAK_TEXT,
                        title = "Aviso de enfoque",
                        param1 = "Iniciando bloque de enfoque de 25 minutos. ¡A por todas!"
                    ),
                    ShortcutAction(
                        type = ActionType.SET_TIMER,
                        title = "Cuenta de 25 minutos",
                        param1 = "1500", // 25 min in seconds
                        param2 = "Pomodoro Enfoque"
                    ),
                    ShortcutAction(
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Notificación Enfoque",
                        param1 = "Modo Enfoque",
                        param2 = "Temporizador de 25 min iniciado"
                    )
                )
            ),
            isFavorite = false
        )

        val s6 = ShortcutEntity(
            id = 6L,
            title = "Calculadora Rápida de Propina",
            description = "Calcula el 15% o 10% de propina al instante",
            colorHex = "#8B5CF6", // Purple
            iconKey = "calculate",
            category = "Utilidades",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.QUICK_CALCULATOR,
                        title = "Cálculo 15%",
                        param1 = "50", // Base amount
                        param2 = "15"  // Percentage
                    )
                )
            ),
            isFavorite = false
        )

        return listOf(s1, s2, s3, s4, s5, s6)
    }

    fun getGalleryTemplates(): List<ShortcutEntity> {
        return listOf(
            ShortcutEntity(
                title = "Alerta SOS con Linterna y Voz",
                description = "Emite pulso SOS y anuncia alarma sonora",
                colorHex = "#EF4444",
                iconKey = "warning",
                category = "Emergencias",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Vibración SOS",
                            param1 = "sos"
                        ),
                        ShortcutAction(
                            type = ActionType.TOGGLE_FLASHLIGHT,
                            title = "Encender linterna",
                            param1 = "on"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Anuncio de emergencia",
                            param1 = "¡Alerta activada! Por favor preste atención."
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "Copiar Plantilla de Correo",
                description = "Copia texto formal al portapapeles y notifica",
                colorHex = "#0EA5E9",
                iconKey = "content_copy",
                category = "Productividad",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.COPY_CLIPBOARD,
                            title = "Copiar firma",
                            param1 = "Saludos cordiales,\nEnviado desde mis Atajos de Android"
                        ),
                        ShortcutAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Copiado",
                            param1 = "Portapapeles",
                            param2 = "Plantilla copiada con éxito"
                        ),
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Pulso",
                            param1 = "80"
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "Compartir Estado de Trabajo",
                description = "Abre el selector para compartir tu estado actual",
                colorHex = "#14B8A6",
                iconKey = "share",
                category = "Comunicación",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.SHARE_TEXT,
                            title = "Compartir texto",
                            param1 = "Actualmente en reunión o modo trabajo intensivo. Responderé pronto."
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "Pausa Activa de 5 Minutos",
                description = "Te recuerda levantarte, estirar y beber agua",
                colorHex = "#06B6D4",
                iconKey = "alarm",
                category = "Salud y Bienestar",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Mensaje de salud",
                            param1 = "Hora de tu pausa activa. Estira los brazos, relaja la vista y bebe un vaso de agua."
                        ),
                        ShortcutAction(
                            type = ActionType.SET_TIMER,
                            title = "Temporizador 5 min",
                            param1 = "300",
                            param2 = "Pausa Activa"
                        )
                    )
                )
            )
        )
    }

    fun getDefaultAutomations(): List<AutomationEntity> {
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
                isEnabled = false,
                notifyWhenRun = true
            )
        )
    }
}
