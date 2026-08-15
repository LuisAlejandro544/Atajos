package com.example.ui.components.automations

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationEntity
import com.example.data.model.ShortcutEntity
import com.example.data.model.TriggerType

/**
 * Diálogo modal desacoplado para configurar y dar de alta una nueva automatización personal.
 */
@Composable
fun NewAutomationDialog(
    availableShortcuts: List<ShortcutEntity>,
    onDismiss: () -> Unit,
    onSave: (AutomationEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedTrigger by remember { mutableStateOf(TriggerType.TIME_OF_DAY) }
    var triggerValue by remember { mutableStateOf("08:00") }
    var selectedShortcutId by remember {
        mutableStateOf(availableShortcuts.firstOrNull()?.id ?: 0L)
    }

    val selectedShortcut = availableShortcuts.firstOrNull { it.id == selectedShortcutId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Nueva Automatización", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej: Rutina de Mañana") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("automation_title_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Selecciona el Disparador:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TriggerType.entries.forEach { trigger ->
                        FilterChip(
                            selected = selectedTrigger == trigger,
                            onClick = {
                                selectedTrigger = trigger
                                if (title.isBlank()) {
                                    title = "Automatización: ${trigger.label}"
                                }
                            },
                            label = { Text(trigger.label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (selectedTrigger == TriggerType.TIME_OF_DAY) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val parts = triggerValue.split(":")
                            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            val isPm = hour >= 12
                            val amPm = if (isPm) "p. m." else "a. m."
                            val displayHour = when {
                                hour == 0 -> 12
                                hour > 12 -> hour - 12
                                else -> hour
                            }
                            val displayStr = String.format("%02d:%02d %s", displayHour, minute, amPm)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Hora: $displayStr",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "($triggerValue)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, h, m ->
                                            triggerValue = String.format("%02d:%02d", h, m)
                                        },
                                        hour,
                                        minute,
                                        false
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Elegir hora", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "Atajo a ejecutar:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (availableShortcuts.isEmpty()) {
                    Text(
                        text = "Primero debes crear al menos un atajo en tu biblioteca.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableShortcuts.take(4).forEach { shortcut ->
                            FilterChip(
                                selected = selectedShortcutId == shortcut.id,
                                onClick = { selectedShortcutId = shortcut.id },
                                label = { Text(shortcut.title) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Auto: ${selectedTrigger.label}" }
                    onSave(
                        AutomationEntity(
                            title = finalTitle,
                            triggerType = selectedTrigger,
                            triggerValue = triggerValue,
                            shortcutId = selectedShortcutId,
                            shortcutTitle = selectedShortcut?.title ?: "Atajo",
                            isEnabled = true
                        )
                    )
                },
                enabled = availableShortcuts.isNotEmpty(),
                modifier = Modifier.testTag("save_automation_button")
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
