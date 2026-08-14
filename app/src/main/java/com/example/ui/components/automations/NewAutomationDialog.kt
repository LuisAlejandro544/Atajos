package com.example.ui.components.automations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                    TriggerType.values().forEach { trigger ->
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
                    OutlinedTextField(
                        value = triggerValue,
                        onValueChange = { triggerValue = it },
                        label = { Text("Hora programada (HH:MM)") },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
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
