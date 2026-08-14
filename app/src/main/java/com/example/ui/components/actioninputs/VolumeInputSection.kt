package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutAction
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VolumeInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStream = action.param1.ifBlank { "music" }
    val currentModeOrLevel = action.param2.ifBlank { "raise" }
    val sliderValue = currentModeOrLevel.toFloatOrNull() ?: 50f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Selector de Canal / Tipo de Sonido
        Text(
            text = "Tipo de sonido / canal:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VolumeChip(
                label = "Multimedia",
                icon = Icons.Default.MusicNote,
                selected = currentStream == "music",
                onClick = { onUpdate(action.copy(param1 = "music")) }
            )
            VolumeChip(
                label = "Notificaciones",
                icon = Icons.Default.Notifications,
                selected = currentStream == "notification",
                onClick = { onUpdate(action.copy(param1 = "notification")) }
            )
            VolumeChip(
                label = "Llamadas",
                icon = Icons.Default.Phone,
                selected = currentStream == "ring",
                onClick = { onUpdate(action.copy(param1 = "ring")) }
            )
            VolumeChip(
                label = "Alarma",
                icon = Icons.Default.Alarm,
                selected = currentStream == "alarm",
                onClick = { onUpdate(action.copy(param1 = "alarm")) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Selector de Acción / Nivel
        Text(
            text = "Acción o nivel de volumen:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VolumeChip(
                label = "Subir (+)",
                icon = Icons.Default.VolumeUp,
                selected = currentModeOrLevel == "raise",
                onClick = { onUpdate(action.copy(param2 = "raise")) }
            )
            VolumeChip(
                label = "Bajar (-)",
                icon = Icons.Default.VolumeDown,
                selected = currentModeOrLevel == "lower",
                onClick = { onUpdate(action.copy(param2 = "lower")) }
            )
            VolumeChip(
                label = "Silenciar (Mute)",
                icon = Icons.Default.VolumeMute,
                selected = currentModeOrLevel == "mute" || currentModeOrLevel == "0",
                onClick = { onUpdate(action.copy(param2 = "mute")) }
            )
            VolumeChip(
                label = "Medio (50%)",
                icon = Icons.Default.VolumeDown,
                selected = currentModeOrLevel == "50",
                onClick = { onUpdate(action.copy(param2 = "50")) }
            )
            VolumeChip(
                label = "Máximo (100%)",
                icon = Icons.Default.VolumeUp,
                selected = currentModeOrLevel == "100" || currentModeOrLevel == "max",
                onClick = { onUpdate(action.copy(param2 = "100")) }
            )
        }

        // Slider de porcentaje de volumen exacto
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = EmeraldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Porcentaje exacto:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = when (currentModeOrLevel) {
                            "raise" -> "Subir (+1 paso)"
                            "lower" -> "Bajar (-1 paso)"
                            "mute" -> "Silenciado (0%)"
                            "max" -> "Máximo (100%)"
                            else -> "${sliderValue.roundToInt()}%"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                }

                Slider(
                    value = sliderValue.coerceIn(0f, 100f),
                    onValueChange = { newValue ->
                        onUpdate(action.copy(param2 = newValue.roundToInt().toString()))
                    },
                    valueRange = 0f..100f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldAccent,
                        activeTrackColor = EmeraldAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("volume_slider")
                )
            }
        }
    }
}

@Composable
private fun VolumeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        shape = RoundedCornerShape(10.dp)
    )
}
