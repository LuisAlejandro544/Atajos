package com.example.ui.components.editors

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.executor.NotificationHelper
import com.example.executor.VariableResolver

@Composable
fun NotificationBlockEditor(
    parameter: String,
    blockColor: Color,
    context: Context,
    onParameterChange: (String) -> Unit
) {
    val currentSound = remember(parameter) {
        if (parameter.startsWith("sound:pop|")) NotificationHelper.SOUND_POP else NotificationHelper.SOUND_DEFAULT
    }
    val textContent = remember(parameter) {
        if (parameter.startsWith("sound:")) {
            val pipeIndex = parameter.indexOf('|')
            if (pipeIndex != -1) parameter.substring(pipeIndex + 1) else parameter
        } else {
            parameter
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))

        // Selector de Sonido
        Text(
            text = "SONIDO DE ALERTA:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = blockColor,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDefSelected = currentSound == NotificationHelper.SOUND_DEFAULT
            val isPopSelected = currentSound == NotificationHelper.SOUND_POP

            // Opción Sistema Estándar
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onParameterChange("sound:default|$textContent")
                    }
                    .border(
                        width = if (isDefSelected) 2.dp else 1.dp,
                        color = if (isDefSelected) blockColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                color = if (isDefSelected) blockColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = if (isDefSelected) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Original",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isDefSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDefSelected) blockColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Opción Pop Audio
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onParameterChange("sound:pop|$textContent")
                    }
                    .border(
                        width = if (isPopSelected) 2.dp else 1.dp,
                        color = if (isPopSelected) blockColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                color = if (isPopSelected) blockColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = if (isPopSelected) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Pop Audio",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isPopSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPopSelected) blockColor else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "GabrielAraujo (CC0)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Botón de escuchar muestra
            IconButton(
                onClick = {
                    NotificationHelper.previewSound(context, currentSound)
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(blockColor.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Escuchar sonido",
                    tint = blockColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = textContent,
            onValueChange = { newText ->
                val prefix = if (currentSound == NotificationHelper.SOUND_POP) "sound:pop|" else "sound:default|"
                onParameterChange("$prefix$newText")
            },
            label = { Text("Texto de la notificación", fontSize = 12.sp) },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Insertar variable dinámica:",
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VariableResolver.AVAILABLE_VARIABLES.forEach { v ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val separator = if (textContent.isNotBlank() && !textContent.endsWith(" ")) " " else ""
                            val newText = "$textContent$separator${v.key}"
                            val prefix = if (currentSound == NotificationHelper.SOUND_POP) "sound:pop|" else "sound:default|"
                            onParameterChange("$prefix$newText")
                        }
                        .border(1.dp, blockColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    color = blockColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+ ${v.key}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = blockColor,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${v.label})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
