package com.example.ui.components.editor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutTrigger

/**
 * Componente modular para configurar el disparador automático nativo del atajo (ej. al conectar/desconectar el cargador).
 */
@Composable
fun ShortcutTriggerSection(
    selectedTriggerKey: String,
    onTriggerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val triggers = listOf(
        TriggerOption(
            trigger = ShortcutTrigger.NONE,
            icon = Icons.Default.TouchApp,
            title = "Solo Manual",
            subtitle = "Se ejecuta al tocarlo en la app o desde el icono de la pantalla de inicio."
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
                text = "Ejecuta este atajo automáticamente sin necesidad de abrir la aplicación.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            triggers.forEach { option ->
                val isSelected = selectedTriggerKey.equals(option.trigger.key, ignoreCase = true)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTriggerSelected(option.trigger.key) }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                            onClick = { onTriggerSelected(option.trigger.key) }
                        )
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
