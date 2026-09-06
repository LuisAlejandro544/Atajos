package com.example.data

import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity

object DefaultShortcuts {

    fun getDefaultShortcuts(): List<ShortcutEntity> = listOf(
        ShortcutEntity(
            title = "Linterna Rápida",
            description = "Flash y registro horario",
            colorHex = "#FF9500",
            iconKey = "FLASH",
            actionType = ActionType.FLASHLIGHT.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.FLASHLIGHT.name, parameter = "", customLabel = "Encender/Apagar Flash"),
                ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "Linterna activada", customLabel = "Registrar estado")
            ),
            parameter = "",
            isFavorite = true,
            category = "Utilidades"
        ),
        ShortcutEntity(
            title = "Ruta a Casa",
            description = "Aviso y navegación en Maps",
            colorHex = "#007AFF",
            iconKey = "MAP",
            actionType = ActionType.MAP_NAV.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "¡Voy en camino a casa!", customLabel = "Copiar aviso"),
                ActionBlock(actionType = ActionType.MAP_NAV.name, parameter = "Casa", customLabel = "Abrir Maps hacia Casa")
            ),
            parameter = "Casa",
            isFavorite = true,
            category = "Viajes"
        ),
        ShortcutEntity(
            title = "Temporizador 5m",
            description = "Temporizador y volumen",
            colorHex = "#FF2D55",
            iconKey = "TIMER",
            actionType = ActionType.SET_TIMER.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.SET_TIMER.name, parameter = "5", customLabel = "Iniciar 5 minutos"),
                ActionBlock(actionType = ActionType.SOUND_SETTINGS.name, parameter = "", customLabel = "Verificar sonido")
            ),
            parameter = "5",
            isFavorite = true,
            category = "Productividad"
        ),
        ShortcutEntity(
            title = "Copiar Mi Correo",
            description = "Portapapeles y compartir",
            colorHex = "#34C759",
            iconKey = "COPY",
            actionType = ActionType.COPY_TEXT.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "contacto@ejemplo.com", customLabel = "Copiar correo"),
                ActionBlock(actionType = ActionType.SHARE_TEXT.name, parameter = "contacto@ejemplo.com", customLabel = "Compartir correo")
            ),
            parameter = "contacto@ejemplo.com",
            isFavorite = false,
            category = "Productividad"
        ),
        ShortcutEntity(
            title = "Buscar en Web",
            description = "Abre Google en navegador",
            colorHex = "#5856D6",
            iconKey = "WEB",
            actionType = ActionType.OPEN_URL.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.OPEN_URL.name, parameter = "https://www.google.com", customLabel = "Abrir Google")
            ),
            parameter = "https://www.google.com",
            isFavorite = false,
            category = "Navegación"
        ),
        ShortcutEntity(
            title = "Ajustar Volumen",
            description = "Fija volumen multimedia al 70%",
            colorHex = "#34C759",
            iconKey = "VOLUME",
            actionType = ActionType.SET_VOLUME.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.SET_VOLUME.name, parameter = "70", customLabel = "Volumen al 70%")
            ),
            parameter = "70",
            isFavorite = false,
            category = "Utilidades"
        ),
        ShortcutEntity(
            title = "Mensaje Rápido",
            description = "Copia y abre envío",
            colorHex = "#00C7BE",
            iconKey = "MESSAGE",
            actionType = ActionType.SEND_MESSAGE.name,
            actions = listOf(
                ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "¡Llego en 5 minutos!", customLabel = "Copiar mensaje"),
                ActionBlock(actionType = ActionType.SEND_MESSAGE.name, parameter = "¡Llego en 5 minutos!", customLabel = "Enviar mensaje")
            ),
            parameter = "¡Llego en 5 minutos!",
            isFavorite = false,
            category = "Comunicación"
        ),
        ShortcutEntity(
            title = "Script Inteligente Lua",
            description = "Lógica y condición horaria",
            colorHex = "#5856D6",
            iconKey = "CODE",
            actionType = ActionType.LUA_SCRIPT.name,
            actions = listOf(
                ActionBlock(
                    actionType = ActionType.LUA_SCRIPT.name,
                    parameter = "local hora = get_hour()\nif hora >= 19 or hora < 7 then\n  flashlight()\n  return 'Noche (hora ' .. hora .. '): linterna'\nelse\n  copy('¡Buen día desde script Lua!')\n  return 'Día (hora ' .. hora .. '): saludo copiado'\nend",
                    customLabel = "Ejecutar lógica condicional Lua"
                )
            ),
            parameter = "local hora = get_hour()\nif hora >= 19 or hora < 7 then\n  flashlight()\n  return 'Noche (hora ' .. hora .. '): linterna'\nelse\n  copy('¡Buen día desde script Lua!')\n  return 'Día (hora ' .. hora .. '): saludo copiado'\nend",
            isFavorite = true,
            category = "Productividad"
        ),
        ShortcutEntity(
            title = "Aviso de Voz",
            description = "Lee un mensaje con la voz del sistema",
            colorHex = "#FF2D55",
            iconKey = "SPEAK",
            actionType = ActionType.SPEAK.name,
            parameter = "Atajo ejecutado a las {hora}. Batería al {bateria} por ciento.",
            isFavorite = true,
            category = "Utilidades"
        ),
        ShortcutEntity(
            title = "Notificación de Estado",
            description = "Aviso prioritario con hora y batería",
            colorHex = "#FF9500",
            iconKey = "NOTIFICATION",
            actionType = ActionType.NOTIFICATION.name,
            actions = listOf(
                ActionBlock(
                    actionType = ActionType.NOTIFICATION.name,
                    parameter = "sound:pop|¡Atención! Son las {hora} ({dia}) y tu batería está al {bateria}%.",
                    customLabel = "Lanzar Notificación con Pop y Variables"
                ),
                ActionBlock(
                    actionType = ActionType.SPEAK.name,
                    parameter = "Aviso recibido a las {hora}",
                    customLabel = "Confirmación por voz"
                )
            ),
            parameter = "sound:pop|¡Atención! Son las {hora} ({dia}) y tu batería está al {bateria}%.",
            isFavorite = true,
            category = "Utilidades"
        ),
        ShortcutEntity(
            title = "Brillo Óptimo",
            description = "Fija el brillo de pantalla al 80%",
            colorHex = "#007AFF",
            iconKey = "BRIGHTNESS",
            actionType = ActionType.SET_BRIGHTNESS.name,
            actions = listOf(
                ActionBlock(
                    actionType = ActionType.SET_BRIGHTNESS.name,
                    parameter = "80",
                    customLabel = "Ajustar brillo al 80%"
                )
            ),
            parameter = "80",
            isFavorite = false,
            category = "Ajustes"
        ),
        ShortcutEntity(
            title = "Aviso con Confirmación",
            description = "Pregunta antes de continuar con la acción",
            colorHex = "#AF52DE",
            iconKey = "PROMPT",
            actionType = ActionType.USER_INTERACTION.name,
            actions = listOf(
                ActionBlock(
                    actionType = ActionType.USER_INTERACTION.name,
                    parameter = """{"promptMessage":"¿Deseas continuar con la ejecución del atajo?","designType":"modal","keyword":"Si","button1Text":"Continuar","button2Text":"Cancelar"}""",
                    customLabel = "Confirmación de usuario"
                ),
                ActionBlock(
                    actionType = ActionType.SPEAK.name,
                    parameter = "Confirmación recibida. Son las {hora}.",
                    customLabel = "Anuncio por voz"
                )
            ),
            parameter = """{"promptMessage":"¿Deseas continuar con la ejecución del atajo?","designType":"modal","keyword":"Si","button1Text":"Continuar","button2Text":"Cancelar"}""",
            isFavorite = true,
            category = "Utilidades"
        )
    )
}
