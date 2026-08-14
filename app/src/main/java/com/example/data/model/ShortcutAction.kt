package com.example.data.model

enum class ActionType(
    val displayName: String,
    val categoryName: String,
    val description: String,
    val iconKey: String
) {
    SPEAK_TEXT(
        displayName = "Pronunciar texto (Voz)",
        categoryName = "Sistema y Audio",
        description = "Lee en voz alta cualquier mensaje usando el motor de voz",
        iconKey = "record_voice_over"
    ),
    TOGGLE_FLASHLIGHT(
        displayName = "Linterna (Flash)",
        categoryName = "Dispositivo",
        description = "Enciende, apaga o alterna la linterna del teléfono",
        iconKey = "flashlight_on"
    ),
    VIBRATE(
        displayName = "Vibración háptica",
        categoryName = "Dispositivo",
        description = "Emite una respuesta de vibración (simple, doble o SOS)",
        iconKey = "vibration"
    ),
    SHOW_NOTIFICATION(
        displayName = "Mostrar notificación",
        categoryName = "Alertas",
        description = "Envía una notificación al centro de notificaciones",
        iconKey = "notifications"
    ),
    OPEN_URL(
        displayName = "Abrir sitio web",
        categoryName = "Navegación",
        description = "Abre una página web en tu navegador predeterminado",
        iconKey = "language"
    ),
    SEARCH_WEB(
        displayName = "Buscar en Google",
        categoryName = "Navegación",
        description = "Realiza una búsqueda inmediata en el buscador",
        iconKey = "search"
    ),
    COPY_CLIPBOARD(
        displayName = "Copiar al portapapeles",
        categoryName = "Productividad",
        description = "Copia un texto definido al portapapeles del móvil",
        iconKey = "content_copy"
    ),
    SEND_WHATSAPP(
        displayName = "Mensaje por WhatsApp",
        categoryName = "Comunicación",
        description = "Abre WhatsApp con un mensaje y contacto predefinido",
        iconKey = "chat"
    ),
    SEND_SMS(
        displayName = "Enviar SMS",
        categoryName = "Comunicación",
        description = "Prepara un SMS rápido a un número de teléfono",
        iconKey = "sms"
    ),
    SHARE_TEXT(
        displayName = "Compartir texto",
        categoryName = "Comunicación",
        description = "Abre el menú nativo para compartir contenido con otras apps",
        iconKey = "share"
    ),
    SET_TIMER(
        displayName = "Temporizador / Alarma",
        categoryName = "Productividad",
        description = "Inicia una cuenta atrás o abre el temporizador del reloj",
        iconKey = "timer"
    ),
    WAIT_DELAY(
        displayName = "Esperar segundos",
        categoryName = "Scripting",
        description = "Pausa la ejecución del atajo antes de la siguiente acción",
        iconKey = "hourglass_empty"
    ),
    QUICK_CALCULATOR(
        displayName = "Calcular propina / porcentaje",
        categoryName = "Utilidades",
        description = "Calcula rápidamente propinas o porcentajes y los muestra",
        iconKey = "calculate"
    ),
    LAUNCH_APP(
        displayName = "Abrir Juego o Aplicación",
        categoryName = "Aplicaciones y Juegos",
        description = "Inicia directamente un juego o aplicación instalada en tu teléfono",
        iconKey = "sports_esports"
    ),
    SET_BRIGHTNESS(
        displayName = "Brillo de pantalla",
        categoryName = "Dispositivo",
        description = "Ajusta, sube o baja el nivel de brillo de la pantalla",
        iconKey = "brightness_6"
    ),
    SET_VOLUME(
        displayName = "Volumen / Sonido",
        categoryName = "Sistema y Audio",
        description = "Sube, baja, silencia o ajusta el volumen de multimedia, llamadas o alarmas",
        iconKey = "volume_up"
    ),
    OPEN_CAMERA(
        displayName = "Abrir Cámara Directa",
        categoryName = "Dispositivo",
        description = "Abre directamente la cámara en modo Foto, Selfie o Grabación de Vídeo",
        iconKey = "camera_alt"
    )
}

data class ShortcutAction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ActionType,
    val title: String = type.displayName,
    val param1: String = "", // e.g. text to speak / URL / search query / phone
    val param2: String = "", // e.g. message body / extra setting / duration
    val param3: String = ""  // e.g. mode (on/off/toggle)
)
