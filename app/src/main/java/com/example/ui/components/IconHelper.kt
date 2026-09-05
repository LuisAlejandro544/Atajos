package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector

data class ShortcutIconItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object ShortcutIconHelper {
    val availableIcons = listOf(
        ShortcutIconItem("FLASH", "Linterna", Icons.Filled.Bolt),
        ShortcutIconItem("APPS", "Aplicación", Icons.Filled.Apps),
        ShortcutIconItem("VOLUME", "Volumen", Icons.Filled.Tune),
        ShortcutIconItem("MAP", "Ubicación", Icons.Filled.Place),
        ShortcutIconItem("TIMER", "Temporizador", Icons.Filled.Timer),
        ShortcutIconItem("COPY", "Portapapeles", Icons.Filled.ContentCopy),
        ShortcutIconItem("WEB", "Internet", Icons.Filled.Language),
        ShortcutIconItem("SOUND", "Sonido", Icons.Filled.VolumeUp),
        ShortcutIconItem("MESSAGE", "Mensaje", Icons.Filled.Chat),
        ShortcutIconItem("SHARE", "Compartir", Icons.Filled.Share),
        ShortcutIconItem("SPEAK", "Texto a Voz", Icons.Filled.RecordVoiceOver),
        ShortcutIconItem("WAIT", "Pausa", Icons.Filled.HourglassTop),
        ShortcutIconItem("CODE", "Código Lua", Icons.Filled.Code)
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons.firstOrNull { it.key == key }?.icon ?: Icons.Filled.Bolt
    }

    val availableColors = listOf(
        "#007AFF" to "Azul",
        "#FF9500" to "Naranja",
        "#34C759" to "Verde",
        "#FF2D55" to "Rosa",
        "#AF52DE" to "Púrpura",
        "#5856D6" to "Índigo",
        "#00C7BE" to "Turquesa",
        "#FF3B30" to "Rojo"
    )
}
