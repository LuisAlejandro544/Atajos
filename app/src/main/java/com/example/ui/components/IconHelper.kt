package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    val availableIcons = listOf(
        "flash_on" to Icons.Default.FlashOn,
        "sports_esports" to Icons.Default.SportsEsports,
        "flashlight_on" to Icons.Default.FlashlightOn,
        "record_voice_over" to Icons.Default.RecordVoiceOver,
        "chat" to Icons.Default.Chat,
        "sms" to Icons.Default.Sms,
        "search" to Icons.Default.Search,
        "language" to Icons.Default.Language,
        "http" to Icons.Default.Language,
        "webhook" to Icons.Default.Language,
        "timer" to Icons.Default.Timer,
        "alarm" to Icons.Default.Alarm,
        "content_copy" to Icons.Default.ContentCopy,
        "share" to Icons.Default.Share,
        "vibration" to Icons.Default.Vibration,
        "notifications" to Icons.Default.Notifications,
        "notifications_active" to Icons.Default.NotificationsActive,
        "camera_alt" to Icons.Default.CameraAlt,
        "camera_front" to Icons.Default.CameraFront,
        "camera_rear" to Icons.Default.CameraRear,
        "brightness_6" to Icons.Default.Brightness6,
        "volume_up" to Icons.Default.VolumeUp,
        "volume_down" to Icons.Default.VolumeDown,
        "volume_off" to Icons.Default.VolumeOff,
        "calculate" to Icons.Default.Calculate,
        "hourglass_empty" to Icons.Default.HourglassEmpty,
        "touch_app" to Icons.Default.TouchApp,
        "lightbulb" to Icons.Default.Lightbulb,
        "warning" to Icons.Default.Warning,
        "widgets" to Icons.Default.Widgets
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons.firstOrNull { it.first == key }?.second ?: Icons.Default.PlayArrow
    }
}
