package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    val availableIcons = listOf(
        "flash_on" to Icons.Default.FlashOn,
        "flashlight_on" to Icons.Default.FlashlightOn,
        "record_voice_over" to Icons.Default.RecordVoiceOver,
        "chat" to Icons.Default.Chat,
        "sms" to Icons.Default.Sms,
        "search" to Icons.Default.Search,
        "language" to Icons.Default.Language,
        "timer" to Icons.Default.Timer,
        "alarm" to Icons.Default.Alarm,
        "content_copy" to Icons.Default.ContentCopy,
        "share" to Icons.Default.Share,
        "vibration" to Icons.Default.Vibration,
        "notifications" to Icons.Default.Notifications,
        "calculate" to Icons.Default.Calculate,
        "hourglass_empty" to Icons.Default.HourglassEmpty,
        "lightbulb" to Icons.Default.Lightbulb,
        "warning" to Icons.Default.Warning,
        "widgets" to Icons.Default.Widgets
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons.firstOrNull { it.first == key }?.second ?: Icons.Default.PlayArrow
    }
}
