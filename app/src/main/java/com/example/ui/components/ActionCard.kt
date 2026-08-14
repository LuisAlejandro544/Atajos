package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.ui.theme.IndigoPrimary

@Composable
fun ActionCard(
    stepIndex: Int,
    action: ShortcutAction,
    totalSteps: Int,
    onUpdate: (ShortcutAction) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("action_card_$stepIndex"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Step badge, Action Name, Reorder & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stepIndex + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = action.type.displayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = action.type.categoryName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stepIndex > 0) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Subir acción",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (stepIndex < totalSteps - 1) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Bajar acción",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_action_$stepIndex")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar acción",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action-specific parameter configurations
            when (action.type) {
                ActionType.SPEAK_TEXT -> {
                    OutlinedTextField(
                        value = action.param1,
                        onValueChange = { onUpdate(action.copy(param1 = it)) },
                        label = { Text("Texto para leer en voz alta") },
                        placeholder = { Text("Ej: ¡Hola! Iniciando jornada...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ActionType.TOGGLE_FLASHLIGHT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentMode = action.param1.ifBlank { "toggle" }
                        listOf("toggle" to "Alternar", "on" to "Encender", "off" to "Apagar").forEach { (mode, label) ->
                            FilterChip(
                                selected = currentMode == mode,
                                onClick = { onUpdate(action.copy(param1 = mode)) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                ActionType.VIBRATE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentPattern = action.param2.ifBlank { "single" }
                        listOf("single" to "Pulso Simple", "double" to "Doble Pulso", "sos" to "Código SOS").forEach { (pattern, label) ->
                            FilterChip(
                                selected = currentPattern == pattern,
                                onClick = { onUpdate(action.copy(param2 = pattern)) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                ActionType.SHOW_NOTIFICATION -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = action.param1,
                            onValueChange = { onUpdate(action.copy(param1 = it)) },
                            label = { Text("Título de la notificación") },
                            placeholder = { Text("Atajos") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = action.param2,
                            onValueChange = { onUpdate(action.copy(param2 = it)) },
                            label = { Text("Mensaje de la notificación") },
                            placeholder = { Text("Tarea completada") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                ActionType.OPEN_URL -> {
                    OutlinedTextField(
                        value = action.param1,
                        onValueChange = { onUpdate(action.copy(param1 = it)) },
                        label = { Text("URL del sitio web") },
                        placeholder = { Text("https://ejemplo.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ActionType.SEARCH_WEB -> {
                    OutlinedTextField(
                        value = action.param1,
                        onValueChange = { onUpdate(action.copy(param1 = it)) },
                        label = { Text("Término de búsqueda") },
                        placeholder = { Text("Ej: el tiempo en Madrid") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ActionType.COPY_CLIPBOARD -> {
                    OutlinedTextField(
                        value = action.param1,
                        onValueChange = { onUpdate(action.copy(param1 = it)) },
                        label = { Text("Texto para copiar al portapapeles") },
                        placeholder = { Text("Texto que deseas guardar") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ActionType.SEND_WHATSAPP -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = action.param1,
                            onValueChange = { onUpdate(action.copy(param1 = it)) },
                            label = { Text("Número de teléfono (opcional)") },
                            placeholder = { Text("Ej: +34600000000 o vacío para elegir") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = action.param2,
                            onValueChange = { onUpdate(action.copy(param2 = it)) },
                            label = { Text("Mensaje de WhatsApp") },
                            placeholder = { Text("Mensaje predefinido") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                ActionType.SEND_SMS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = action.param1,
                            onValueChange = { onUpdate(action.copy(param1 = it)) },
                            label = { Text("Número de teléfono") },
                            placeholder = { Text("Número destinatario") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = action.param2,
                            onValueChange = { onUpdate(action.copy(param2 = it)) },
                            label = { Text("Texto del SMS") },
                            placeholder = { Text("Mensaje SMS") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                ActionType.SHARE_TEXT -> {
                    OutlinedTextField(
                        value = action.param1,
                        onValueChange = { onUpdate(action.copy(param1 = it)) },
                        label = { Text("Texto a compartir") },
                        placeholder = { Text("Contenido para enviar a otras apps") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ActionType.SET_TIMER -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = action.param1,
                            onValueChange = { onUpdate(action.copy(param1 = it)) },
                            label = { Text("Segundos") },
                            placeholder = { Text("300") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = action.param2,
                            onValueChange = { onUpdate(action.copy(param2 = it)) },
                            label = { Text("Etiqueta") },
                            placeholder = { Text("Temporizador") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                ActionType.WAIT_DELAY -> {
                    OutlinedTextField(
                        value = action.param1,
                        onValueChange = { onUpdate(action.copy(param1 = it)) },
                        label = { Text("Segundos de espera") },
                        placeholder = { Text("2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ActionType.QUICK_CALCULATOR -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = action.param1,
                            onValueChange = { onUpdate(action.copy(param1 = it)) },
                            label = { Text("Importe (€/$)") },
                            placeholder = { Text("50") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = action.param2,
                            onValueChange = { onUpdate(action.copy(param2 = it)) },
                            label = { Text("% Porcentaje") },
                            placeholder = { Text("15") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}
