package com.example.ui.viewmodel

import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

/**
 * Fábrica de instancias por defecto para cada tipo de acción agregada en el editor.
 */
object DefaultActionFactory {

    fun createDefaultAction(type: ActionType): ShortcutAction {
        return when (type) {
            ActionType.SPEAK_TEXT -> ShortcutAction(
                type = type,
                title = "Pronunciar mensaje",
                param1 = "¡Acción ejecutada con éxito!"
            )
            ActionType.TOGGLE_FLASHLIGHT -> ShortcutAction(
                type = type,
                title = "Alternar Linterna",
                param1 = "toggle"
            )
            ActionType.VIBRATE -> ShortcutAction(
                type = type,
                title = "Vibración háptica",
                param1 = "250",
                param2 = "heavy"
            )
            ActionType.SHOW_NOTIFICATION -> ShortcutAction(
                type = type,
                title = "Mostrar Notificación",
                param1 = "Atajos",
                param2 = "Tarea automatizada completada"
            )
            ActionType.OPEN_URL -> ShortcutAction(
                type = type,
                title = "Abrir Enlace Web",
                param1 = "https://google.com"
            )
            ActionType.SEARCH_WEB -> ShortcutAction(
                type = type,
                title = "Buscar en la Web",
                param1 = "Noticias del día"
            )
            ActionType.COPY_CLIPBOARD -> ShortcutAction(
                type = type,
                title = "Copiar al Portapapeles",
                param1 = "Texto copiado"
            )
            ActionType.SEND_WHATSAPP -> ShortcutAction(
                type = type,
                title = "Enviar por WhatsApp",
                param1 = "",
                param2 = "¡Hola! Te escribo desde un atajo."
            )
            ActionType.SEND_SMS -> ShortcutAction(
                type = type,
                title = "Enviar SMS",
                param1 = "",
                param2 = "Mensaje automático"
            )
            ActionType.SHARE_TEXT -> ShortcutAction(
                type = type,
                title = "Compartir texto",
                param1 = "Compartido desde Atajos"
            )
            ActionType.SET_TIMER -> ShortcutAction(
                type = type,
                title = "Iniciar Temporizador",
                param1 = "300",
                param2 = "Mi Temporizador"
            )
            ActionType.WAIT_DELAY -> ShortcutAction(
                type = type,
                title = "Pausa / Esperar",
                param1 = "2"
            )
            ActionType.QUICK_CALCULATOR -> ShortcutAction(
                type = type,
                title = "Calcular Propina",
                param1 = "50",
                param2 = "15"
            )
            ActionType.LAUNCH_APP -> ShortcutAction(
                type = type,
                title = "Abrir Juego o App",
                param1 = "",
                param2 = ""
            )
            ActionType.SET_BRIGHTNESS -> ShortcutAction(
                type = type,
                title = "Ajustar Brillo",
                param1 = "50",
                param2 = "manual"
            )
            ActionType.SET_VOLUME -> ShortcutAction(
                type = type,
                title = "Ajustar Volumen",
                param1 = "music",
                param2 = "raise"
            )
            ActionType.OPEN_CAMERA -> ShortcutAction(
                type = type,
                title = "Abrir Cámara",
                param1 = "photo",
                param2 = "back"
            )
        }
    }
}
