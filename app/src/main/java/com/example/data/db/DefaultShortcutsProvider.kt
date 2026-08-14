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

        val s7 = ShortcutEntity(
            id = 7L,
            title = "Aviso al Conectar Cargador",
            description = "Se activa automáticamente al enchufar el celular a la corriente y anuncia el nivel de batería",
            colorHex = "#10B981", // Emerald
            iconKey = "battery_charging_full",
            category = "Sistema",
            trigger = "POWER_CONNECTED",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.VIBRATE,
                        title = "Pulso de conexión",
                        param1 = "150",
                        param2 = "double"
                    ),
                    ShortcutAction(
                        type = ActionType.SPEAK_TEXT,
                        title = "Anunciar carga",
                        param1 = "Cargador conectado. Nivel actual de batería: {BATERIA}."
                    ),
                    ShortcutAction(
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Cargador Conectado",
                        param1 = "Cargando ({BATERIA})",
                        param2 = "Iniciada recarga a las {HORA}"
                    )
                )
            ),
            isFavorite = true
        )

        val s8 = ShortcutEntity(
            id = 8L,
            title = "Aviso al Desconectar",
            description = "Se activa al desenchufar el cargador y te recuerda llevar tus pertenencias",
            colorHex = "#F59E0B", // Amber
            iconKey = "power_off",
            category = "Sistema",
            trigger = "POWER_DISCONNECTED",
            actionsJson = ActionJsonHelper.toJson(
                listOf(
                    ShortcutAction(
                        type = ActionType.VIBRATE,
                        title = "Vibración de desconexión",
                        param1 = "200",
                        param2 = "single"
                    ),
                    ShortcutAction(
                        type = ActionType.SPEAK_TEXT,
                        title = "Anuncio de desconexión",
                        param1 = "Cargador desconectado. Batería restante: {BATERIA}."
                    )
                )
            ),
            isFavorite = false
        )

        return listOf(s1, s2, s3, s4, s5, s6, s7, s8)
    }

    fun getGalleryTemplates(): List<ShortcutEntity> {
        return listOf(
            ShortcutEntity(
                title = "🌐 Consultar API Web y Pronunciar",
                description = "Obtiene datos en vivo de una API pública o URL y lee el resultado con Text-To-Speech",
                colorHex = "#0EA5E9",
                iconKey = "http",
                category = "Internet y Web",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.HTTP_REQUEST,
                            title = "Obtener Frase Zen (API GitHub)",
                            param1 = "https://api.github.com/zen",
                            param2 = "GET"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Leer Respuesta Web",
                            param1 = "Frase del día: {RESPUESTA_WEB}"
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🚀 Disparar Webhook con Telemetría",
                description = "Envía una petición POST con nivel de batería y hora actual hacia un webhook",
                colorHex = "#6366F1",
                iconKey = "http",
                category = "Internet y Web",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.HTTP_REQUEST,
                            title = "Enviar Webhook POST",
                            param1 = "https://httpbin.org/post",
                            param2 = "POST",
                            param3 = "{\"evento\": \"inicio_jornada\", \"bateria\": \"{BATERIA}\", \"hora\": \"{HORA}\"}"
                        ),
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Confirmación Háptica",
                            param1 = "100",
                            param2 = "click"
                        ),
                        ShortcutAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Webhook Enviado",
                            param1 = "Petición Web Completada",
                            param2 = "Estado: HTTP {HTTP_STATUS}"
                        )
                    )
                )
            ),
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
            ),
            ShortcutEntity(
                title = "⚡ Rutina Automática al Conectar",
                description = "Se activa en segundo plano al enchufar el móvil: baja el brillo y avisa por voz la batería",
                colorHex = "#10B981",
                iconKey = "battery_charging_full",
                category = "Automatización",
                trigger = "POWER_CONNECTED",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Vibración de carga",
                            param1 = "150",
                            param2 = "double"
                        ),
                        ShortcutAction(
                            type = ActionType.SET_BRIGHTNESS,
                            title = "Ajustar brillo óptimo",
                            param1 = "30"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Informe de Carga",
                            param1 = "Cargador conectado. Batería al {BATERIA}."
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🔌 Aviso Automático al Desconectar",
                description = "Se activa en segundo plano al desenchufar el cargador: vibra y anuncia el estado",
                colorHex = "#F59E0B",
                iconKey = "power_off",
                category = "Automatización",
                trigger = "POWER_DISCONNECTED",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Pulso háptico",
                            param1 = "200",
                            param2 = "single"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Aviso al desconectar",
                            param1 = "Cargador desconectado. Nivel de batería: {BATERIA}."
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🪫 Modo Ahorro: Alerta Batería Baja",
                description = "Se activa en segundo plano cuando la batería baja de 15%: reduce brillo y alerta por voz",
                colorHex = "#EF4444",
                iconKey = "battery_alert",
                category = "Automatización",
                trigger = "BATTERY_LOW",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Vibración de alerta",
                            param1 = "300",
                            param2 = "double"
                        ),
                        ShortcutAction(
                            type = ActionType.SET_BRIGHTNESS,
                            title = "Bajar brillo al 10%",
                            param1 = "10"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Aviso de batería crítica",
                            param1 = "¡Atención! Nivel de batería crítico al {BATERIA}. Conecta el cargador."
                        )
                    )
                )
            ),
            ShortcutEntity(
                title = "🔋 Aviso de Carga Completa (100%)",
                description = "Se activa automáticamente al llegar al 100% de carga para proteger la salud de la batería",
                colorHex = "#10B981",
                iconKey = "battery_full",
                category = "Automatización",
                trigger = "BATTERY_FULL",
                actionsJson = ActionJsonHelper.toJson(
                    listOf(
                        ShortcutAction(
                            type = ActionType.VIBRATE,
                            title = "Pulso de confirmación",
                            param1 = "200",
                            param2 = "single"
                        ),
                        ShortcutAction(
                            type = ActionType.SPEAK_TEXT,
                            title = "Aviso de carga completa",
                            param1 = "Batería cargada al 100%. Puedes desconectar el cargador."
                        ),
                        ShortcutAction(
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Batería 100%",
                            param1 = "Carga Completa",
                            param2 = "El dispositivo ha finalizado su recarga completa."
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
