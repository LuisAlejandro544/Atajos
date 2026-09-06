package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActionType(val label: String, val defaultParam: String, val paramLabel: String) {
    FLASHLIGHT("Alternar Linterna", "", "Sin parámetros necesarios"),
    OPEN_APP("Abrir Aplicación", "", "Paquete o aplicación a abrir"),
    OPEN_URL("Abrir Sitio Web", "https://google.com", "URL a abrir (ej: https://...)"),
    SET_VOLUME("Ajustar Volumen", "70", "Nivel de volumen en porcentaje (0-100%)"),
    COPY_TEXT("Copiar al Portapapeles", "Texto importante de ejemplo", "Texto a copiar"),
    MAP_NAV("Navegar en Mapas", "Estación Central", "Destino o dirección"),
    SET_TIMER("Ajustar Temporizador", "5", "Minutos para el temporizador"),
    SEND_MESSAGE("Mensaje Rápido", "¡Hola! Te escribo en un momento.", "Mensaje a enviar"),
    SOUND_SETTINGS("Ajustes de Sonido", "", "Abre el panel de audio del sistema"),
    SHARE_TEXT("Compartir Texto", "¡Mira este atajo increíble!", "Texto a compartir"),
    SPEAK("Texto a Voz", "Secuencia de atajos completada a las {hora}", "Texto que leerá la voz del sistema"),
    NOTIFICATION("Mostrar Notificación", "Atajo completado a las {hora} | Batería: {bateria}%", "Texto de la notificación (admite {hora}, {fecha}, {bateria}, {portapapeles})"),
    SET_BRIGHTNESS("Brillo de Pantalla", "80", "Nivel de brillo en porcentaje (0-100%)"),
    WAIT("Esperar (Pausa)", "1003", "Tiempo de espera en milisegundos (ej: 1003 o 2500)"),
    LUA_SCRIPT("Script en Lua", "local hora = get_hour()\nif hora >= 20 then\n  flashlight()\n  return 'Hora nocturna: linterna'\nelse\n  copy('¡Hola desde Lua!')\n  return 'Hora diurna: texto copiado'\nend", "Código Lua a ejecutar"),
    USER_INTERACTION("Preguntar al Usuario", "{\"designType\":\"MODAL\",\"promptTitle\":\"¿Deseas continuar?\",\"promptMessage\":\"Confirma para ejecutar los siguientes pasos del atajo.\",\"inputMode\":\"BUTTONS\",\"expectedKeyword\":\"Si\",\"buttonPositiveText\":\"Continuar\",\"buttonNegativeText\":\"Cancelar\"}", "Confirmación o palabra clave antes de continuar")
}

enum class TriggerType(val label: String, val description: String) {
    MANUAL("Manual", "Se ejecuta al pulsar la tarjeta"),
    CHARGER_CONNECTED("Al conectar cargador", "⚡ Se dispara al enchufar el cargador"),
    CHARGER_DISCONNECTED("Al desconectar cargador", "🔋 Se dispara al desenchufar el cargador")
}

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val colorHex: String,
    val iconKey: String,
    val actionType: String,
    val actions: List<ActionBlock> = emptyList(),
    val parameter: String = "",
    val isFavorite: Boolean = false,
    val category: String = "General",
    val executionCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false,
    val triggerType: String = TriggerType.MANUAL.name,
    val backgroundImageUri: String? = null
)

