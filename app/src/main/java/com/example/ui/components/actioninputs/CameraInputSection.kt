package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutAction
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMode = action.param1.ifBlank { "photo" }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Modo de disparo inicial:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CameraModeChip(
                label = "Foto estándar",
                icon = Icons.Default.CameraAlt,
                selected = currentMode == "photo" || currentMode == "back",
                onClick = { onUpdate(action.copy(param1 = "photo", param2 = "back")) }
            )
            CameraModeChip(
                label = "Selfie (Frontal)",
                icon = Icons.Default.CameraFront,
                selected = currentMode == "selfie" || currentMode == "front",
                onClick = { onUpdate(action.copy(param1 = "selfie", param2 = "front")) }
            )
            CameraModeChip(
                label = "Grabación de Vídeo",
                icon = Icons.Default.Videocam,
                selected = currentMode == "video" || currentMode == "video_capture",
                onClick = { onUpdate(action.copy(param1 = "video", param2 = "video")) }
            )
        }

        // Mensaje de transparencia y privacidad
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Abre la cámara nativa de tu dispositivo al instante sin recopilar ni subir fotos a ningún servidor.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CameraModeChip(
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
