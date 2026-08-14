package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationEntity
import com.example.data.model.ShortcutEntity
import com.example.data.model.TriggerType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary

@Composable
fun AutomationsScreen(
    automations: List<AutomationEntity>,
    availableShortcuts: List<ShortcutEntity>,
    showDialog: Boolean,
    onToggleAutomation: (Long, Boolean) -> Unit,
    onDeleteAutomation: (Long) -> Unit,
    onTestRunAutomation: (AutomationEntity) -> Unit,
    onOpenNewDialog: () -> Unit,
    onCloseDialog: () -> Unit,
    onSaveAutomation: (AutomationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoMode,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Automatizaciones Personales",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ejecuta atajos automáticamente según eventos de tu móvil o programación.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            if (automations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoMode,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin automatizaciones creadas",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Crea tu primera automatización con el botón de abajo",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(automations, key = { it.id }) { automation ->
                    AutomationCard(
                        automation = automation,
                        onToggle = { enabled -> onToggleAutomation(automation.id, enabled) },
                        onDelete = { onDeleteAutomation(automation.id) },
                        onTestRun = { onTestRunAutomation(automation) }
                    )
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = onOpenNewDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("create_automation_fab"),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Nueva Automatización") },
            containerColor = IndigoPrimary,
            contentColor = Color.White
        )
    }

    if (showDialog) {
        NewAutomationDialog(
            availableShortcuts = availableShortcuts,
            onDismiss = onCloseDialog,
            onSave = onSaveAutomation
        )
    }
}

@Composable
fun AutomationCard(
    automation: AutomationEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onTestRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    val triggerIcon: ImageVector = when (automation.triggerType) {
        TriggerType.TIME_OF_DAY -> Icons.Default.Schedule
        TriggerType.CHARGER_CONNECTED -> Icons.Default.BatteryChargingFull
        TriggerType.BATTERY_LOW -> Icons.Default.BatteryAlert
        TriggerType.APP_OPENED -> Icons.Default.PlayCircle
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("automation_card_${automation.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (automation.isEnabled) IndigoPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            tint = if (automation.isEnabled) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = automation.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Disparador: ${automation.triggerType.label}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = automation.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("automation_switch_${automation.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action details row
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ejecuta: ${automation.shortcutTitle.ifBlank { "Atajo #${automation.shortcutId}" }}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onTestRun,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("test_automation_${automation.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Probar", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

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
