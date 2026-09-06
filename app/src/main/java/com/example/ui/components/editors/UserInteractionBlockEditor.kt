package com.example.ui.components.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material3.Icon
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
import com.example.data.model.UserInteractionConfig

@Composable
fun UserInteractionBlockEditor(
    parameter: String,
    blockColor: Color,
    onParameterChange: (String) -> Unit
) {
    val config = remember(parameter) {
        UserInteractionConfig.fromJson(parameter)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))

        // Selector de Diseño: Modal vs Notificación
        Text(
            text = "DISEÑO DE INTERACCIÓN:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = blockColor,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isModal = config.designType == UserInteractionConfig.DESIGN_MODAL
            val isNotif = config.designType == UserInteractionConfig.DESIGN_NOTIFICATION

            // Opción 1: Modal Emergente
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onParameterChange(config.copy(designType = UserInteractionConfig.DESIGN_MODAL).toJson())
                    }
                    .border(
                        width = if (isModal) 2.dp else 1.dp,
                        color = if (isModal) blockColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                color = if (isModal) blockColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WebAsset,
                        contentDescription = null,
                        tint = if (isModal) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Modal Emergente",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isModal) FontWeight.Bold else FontWeight.Normal,
                            color = if (isModal) blockColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Opción 2: Notificación
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onParameterChange(config.copy(designType = UserInteractionConfig.DESIGN_NOTIFICATION).toJson())
                    }
                    .border(
                        width = if (isNotif) 2.dp else 1.dp,
                        color = if (isNotif) blockColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                color = if (isNotif) blockColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = if (isNotif) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Notificación",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isNotif) FontWeight.Bold else FontWeight.Normal,
                            color = if (isNotif) blockColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mensaje / Información a mostrar
        OutlinedTextField(
            value = config.promptTitle,
            onValueChange = { newTitle ->
                onParameterChange(config.copy(promptTitle = newTitle).toJson())
            },
            label = { Text("Título de la pregunta", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = config.promptMessage,
            onValueChange = { newMsg ->
                onParameterChange(config.copy(promptMessage = newMsg).toJson())
            },
            label = { Text("Información o descripción detallada", fontSize = 12.sp) },
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (config.designType == UserInteractionConfig.DESIGN_MODAL) {
            // Configuración específica de Modal Emergente
            Text(
                text = "TIPO DE RESPUESTA EN EL MODAL:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = blockColor,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isButtons = config.inputMode == UserInteractionConfig.MODE_BUTTONS
                val isKeyword = config.inputMode == UserInteractionConfig.MODE_KEYWORD

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onParameterChange(config.copy(inputMode = UserInteractionConfig.MODE_BUTTONS).toJson())
                        }
                        .border(
                            width = if (isButtons) 2.dp else 1.dp,
                            color = if (isButtons) blockColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    color = if (isButtons) blockColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "2 Botones de Acción",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isButtons) FontWeight.Bold else FontWeight.Normal,
                            color = if (isButtons) blockColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onParameterChange(config.copy(inputMode = UserInteractionConfig.MODE_KEYWORD).toJson())
                        }
                        .border(
                            width = if (isKeyword) 2.dp else 1.dp,
                            color = if (isKeyword) blockColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    color = if (isKeyword) blockColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Palabra Clave",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isKeyword) FontWeight.Bold else FontWeight.Normal,
                            color = if (isKeyword) blockColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (config.inputMode == UserInteractionConfig.MODE_BUTTONS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = config.buttonPositiveText,
                        onValueChange = {
                            onParameterChange(config.copy(buttonPositiveText = it).toJson())
                        },
                        label = { Text("Botón 1 (Continuar)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = config.buttonNegativeText,
                        onValueChange = {
                            onParameterChange(config.copy(buttonNegativeText = it).toJson())
                        },
                        label = { Text("Botón 2 (Cancelar)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = config.expectedKeyword,
                    onValueChange = {
                        onParameterChange(config.copy(expectedKeyword = it).toJson())
                    },
                    label = { Text("Palabra clave para continuar (ej: Si, OK)", fontSize = 12.sp) },
                    placeholder = { Text("Ej: Si") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Notificación
            OutlinedTextField(
                value = config.expectedKeyword,
                onValueChange = {
                    onParameterChange(config.copy(expectedKeyword = it).toJson())
                },
                label = { Text("Palabra clave para continuar (ej: Si, No, Continuar)", fontSize = 12.sp) },
                placeholder = { Text("Ej: Si") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(blockColor.copy(alpha = 0.1f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = blockColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "El sistema desplegará una notificación interactiva. El atajo solo continuará cuando se responda la palabra clave configurada.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
