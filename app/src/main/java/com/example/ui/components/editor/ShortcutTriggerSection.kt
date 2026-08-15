package com.example.ui.components.editor

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutTrigger
import java.util.Calendar

/**
 * Componente modular para configurar el disparador automático nativo del atajo
 * (disparadores de batería personalizada con selector de porcentaje exacto, hora programada con soporte AM/PM, eventos de carga).
 */
@Composable
fun ShortcutTriggerSection(
    selectedTriggerKey: String,
    onTriggerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrigger = ShortcutTrigger.fromKey(selectedTriggerKey)

    // Estados para valor personalizado de batería y hora
    val initialBatteryPct = if (currentTrigger == ShortcutTrigger.BATTERY_LEVEL) {
        ShortcutTrigger.extractValue(selectedTriggerKey, "50").toIntOrNull() ?: 50
    } else 50

    val initialTime = if (currentTrigger == ShortcutTrigger.TIME_EXACT) {
        ShortcutTrigger.extractValue(selectedTriggerKey, "08:00")
    } else "08:00"

    var customBatteryPct by remember(selectedTriggerKey) {
        mutableFloatStateOf(initialBatteryPct.toFloat())
    }

    var customTimeStr by remember(selectedTriggerKey) {
        mutableStateOf(initialTime)
    }

    val triggerOptions = listOf(
        TriggerOption(
            trigger = ShortcutTrigger.NONE,
            icon = Icons.Default.TouchApp,
            title = "Solo Manual",
            subtitle = "Se ejecuta al tocarlo en la app o desde el icono de la pantalla de inicio."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.TIME_EXACT,
            icon = Icons.Default.Schedule,
            title = "A una hora exacta del día",
            subtitle = "Se ejecuta automáticamente a la hora que elijas todos los días."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.BATTERY_LEVEL,
            icon = Icons.Default.BatteryChargingFull,
            title = "Nivel de batería personalizado",
            subtitle = "Elige el porcentaje exacto al que quieres que inicie el atajo."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.POWER_CONNECTED,
            icon = Icons.Default.BatteryChargingFull,
            title = "Al conectar cargador",
            subtitle = "Se ejecuta en segundo plano cuando el teléfono comienza a cargar corriente."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.POWER_DISCONNECTED,
            icon = Icons.Default.PowerOff,
            title = "Al desconectar cargador",
            subtitle = "Se ejecuta en segundo plano cuando se desenchufa el teléfono."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.POWER_BOTH,
            icon = Icons.Default.Bolt,
            title = "Al conectar o desconectar",
            subtitle = "Se activa ante cualquier cambio en el estado de alimentación."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.BATTERY_LOW,
            icon = Icons.Default.BatteryAlert,
            title = "Batería baja (<15%)",
            subtitle = "Se ejecuta automáticamente cuando la batería desciende al nivel crítico."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.BATTERY_OK,
            icon = Icons.Default.BatterySaver,
            title = "Batería restablecida (>20%)",
            subtitle = "Se ejecuta al recuperar un nivel de batería seguro."
        ),
        TriggerOption(
            trigger = ShortcutTrigger.BATTERY_FULL,
            icon = Icons.Default.BatteryFull,
            title = "Batería cargada al 100%",
            subtitle = "Se ejecuta cuando el dispositivo finaliza la recarga completa."
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            Text(
                text = "Disparador Automático",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ejecuta este atajo automáticamente según eventos del sistema, hora u porcentaje de batería.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            triggerOptions.forEach { option ->
                val isSelected = currentTrigger == option.trigger

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (option.trigger) {
                                ShortcutTrigger.BATTERY_LEVEL -> {
                                    onTriggerSelected("BATTERY_LEVEL:${customBatteryPct.toInt()}")
                                }
                                ShortcutTrigger.TIME_EXACT -> {
                                    onTriggerSelected("TIME_EXACT:$customTimeStr")
                                }
                                else -> {
                                    onTriggerSelected(option.trigger.key)
                                }
                            }
                        }
                        .testTag("trigger_option_${option.trigger.key.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ),
                    border = if (isSelected) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = option.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    when (option.trigger) {
                                        ShortcutTrigger.BATTERY_LEVEL -> {
                                            onTriggerSelected("BATTERY_LEVEL:${customBatteryPct.toInt()}")
                                        }
                                        ShortcutTrigger.TIME_EXACT -> {
                                            onTriggerSelected("TIME_EXACT:$customTimeStr")
                                        }
                                        else -> {
                                            onTriggerSelected(option.trigger.key)
                                        }
                                    }
                                }
                            )
                        }

                        // ── Sub-panel: Selector interactivo de Nivel de Batería Exacto ──────────────────
                        AnimatedVisibility(
                            visible = isSelected && option.trigger == ShortcutTrigger.BATTERY_LEVEL,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Porcentaje objetivo:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${customBatteryPct.toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Slider(
                                    value = customBatteryPct,
                                    onValueChange = {
                                        customBatteryPct = it
                                        onTriggerSelected("BATTERY_LEVEL:${it.toInt()}")
                                    },
                                    valueRange = 5f..100f,
                                    steps = 18, // Pasos de 5%
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("battery_level_slider")
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Pastillas compactas optimizadas para selección rápida de porcentaje
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    listOf(15, 20, 50, 80, 90, 100).forEach { pct ->
                                        val isCurrentPct = customBatteryPct.toInt() == pct
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isCurrentPct) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    width = if (isCurrentPct) 1.2.dp else 0.5.dp,
                                                    color = if (isCurrentPct) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    customBatteryPct = pct.toFloat()
                                                    onTriggerSelected("BATTERY_LEVEL:$pct")
                                                }
                                                .padding(vertical = 7.dp, horizontal = 1.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$pct%",
                                                fontSize = 11.sp,
                                                fontWeight = if (isCurrentPct) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrentPct) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Sub-panel: Selector interactivo de Hora Exacta con AM / PM ──────────────────────────────
                        AnimatedVisibility(
                            visible = isSelected && option.trigger == ShortcutTrigger.TIME_EXACT,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = "Hora programada:",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = formatTimeWithAmPm(customTimeStr),
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "($customTimeStr)",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val parts = customTimeStr.split(":")
                                            val currentHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                            val currentMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                                            val timePicker = TimePickerDialog(
                                                context,
                                                { _, selectedHour, selectedMinute ->
                                                    val formatted = String.format("%02d:%02d", selectedHour, selectedMinute)
                                                    customTimeStr = formatted
                                                    onTriggerSelected("TIME_EXACT:$formatted")
                                                },
                                                currentHour,
                                                currentMinute,
                                                false // Diálogo con selector de AM / PM
                                            )
                                            timePicker.show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("open_time_picker_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cambiar hora", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Horas habituales:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                // Pastillas compactas con formato AM/PM para horas frecuentes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    timePresetsList.forEach { preset ->
                                        val isCurrent = customTimeStr == preset.time24
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isCurrent) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    width = if (isCurrent) 1.2.dp else 0.5.dp,
                                                    color = if (isCurrent) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    customTimeStr = preset.time24
                                                    onTriggerSelected("TIME_EXACT:${preset.time24}")
                                                }
                                                .padding(vertical = 7.dp, horizontal = 1.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = preset.displayLabel,
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class TriggerOption(
    val trigger: ShortcutTrigger,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private data class TimePresetItem(
    val time24: String,
    val displayLabel: String
)

private val timePresetsList = listOf(
    TimePresetItem("07:00", "7:00 AM"),
    TimePresetItem("08:30", "8:30 AM"),
    TimePresetItem("14:00", "2:00 PM"),
    TimePresetItem("20:00", "8:00 PM"),
    TimePresetItem("22:30", "10:30 PM")
)

/**
 * Convierte un formato de hora HH:mm a formato amigable con a. m. / p. m.
 */
fun formatTimeWithAmPm(timeStr: String): String {
    val parts = timeStr.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val isPm = hour >= 12
    val amPm = if (isPm) "p. m." else "a. m."
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%02d:%02d %s", displayHour, minute, amPm)
}
