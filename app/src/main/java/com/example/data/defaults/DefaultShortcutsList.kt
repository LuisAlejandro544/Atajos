package com.example.data.defaults

import com.example.data.model.ActionJsonHelper
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.data.model.ShortcutEntity

/**
 * Proveedor modular de los atajos predeterminados iniciales sembrados en la base de datos.
 */
object DefaultShortcutsList {

    fun get(): List<ShortcutEntity> {
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
}
