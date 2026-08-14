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
                        param1 = "250",
                        param2 = "heavy"
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
            title = "Informe de Estado & Saludo",
            description = "Dice por voz la hora, fecha y nivel actual de batería",
            colorHex = "#4F46E5", // Indigo
            iconKey = "record_voice_over",
            category = "Sistema",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.VIBRATE,
                        title = "Vibración suave",
                        param1 = "100",
                        param2 = "click"
                    ),
                    ShortcutAction(
                        type = ActionType.SPEAK_TEXT,
                        title = "Pronunciar informe dinámico",
                        param1 = "¡Hola! Hoy es {DIA_SEMANA}, son las {HORA} y tu batería está al {BATERIA}.",
                        param2 = "es"
                    ),
                    ShortcutAction(
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Notificación de Estado",
                        param1 = "Reporte {HORA}",
                        param2 = "Batería: {BATERIA} ({ESTADO_BATERIA}) | {DIA_SEMANA}"
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
                        title = "Pulso háptico",
                        param1 = "150",
                        param2 = "double"
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
                title = "📢 Notificar y Leer en Voz Alta",
                description = "Muestra una notificación personalizada y el sintetizador de voz la lee de inmediato",
                colorHex = "#4F46E5",
                iconKey = "notifications_active",
                category = "Alertas",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Aviso de Juego",
                            param1 = "Atajos",
                            param2 = "¡Es la hora de jugar y relajarse!"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Leer Notificación en Voz Alta",
                            param1 = "{ULTIMA_NOTIFICACION}",
                            param2 = "read_notification"
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🌙 Modo Noche: Brillo Bajo & Silencio",
                description = "Baja el brillo de pantalla al 10% y silencia los altavoces para descansar",
                colorHex = "#3B82F6",
                iconKey = "brightness_6",
                category = "Dispositivo",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.SET_BRIGHTNESS,
                            title = "Bajar Brillo al Mínimo",
                            param1 = "10"
                        ),
                        ShortcutAction(
                            type = ActionType.SET_VOLUME,
                            title = "Silenciar Multimedia",
                            param1 = "music",
                            param2 = "mute"
                        ),
                        ShortcutAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Modo Noche Activado",
                            param1 = "Buenas noches",
                            param2 = "Brillo y sonido reducidos para descansar"
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "📸 Cámara Rápida con Linterna",
                description = "Enciende un pulso háptico y abre la cámara lista para capturar fotos",
                colorHex = "#F59E0B",
                iconKey = "camera_alt",
                category = "Dispositivo",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Pulso Háptico",
                            param1 = "100",
                            param2 = "click"
                        ),
                        ShortcutAction(
                            type = ActionType.OPEN_CAMERA,
                            title = "Abrir Cámara Foto",
                            param1 = "photo",
                            param2 = "back"
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🤳 Selfie Instantánea",
                description = "Abre directamente la cámara frontal para una selfie rápida",
                colorHex = "#EC4899",
                iconKey = "camera_front",
                category = "Dispositivo",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.OPEN_CAMERA,
                            title = "Abrir Modo Selfie",
                            param1 = "selfie",
                            param2 = "front"
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🎮 Lanzador de Juego Rápido",
                description = "Emite doble pulso háptico y abre tu juego favorito directamente",
                colorHex = "#8B5CF6",
                iconKey = "sports_esports",
                category = "Juegos",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Vibración Gamer",
                            param1 = "250",
                            param2 = "double"
                        ),
                        ShortcutAction(
                            type = ActionType.LAUNCH_APP,
                            title = "Abrir Juego o App",
                            param1 = "",
                            param2 = ""
                        ),
                        ShortcutAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Modo Juego",
                            param1 = "Atajos Gamer",
                            param2 = "¡A jugar! Sesión iniciada."
                        )
                    )
                )
            ),
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
                            param1 = "100",
                            param2 = "sos"
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
                            title = "Pulso de confirmación",
                            param1 = "80",
                            param2 = "click"
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
