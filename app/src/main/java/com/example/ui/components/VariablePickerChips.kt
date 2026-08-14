package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SystemVariable
import com.example.engine.VariableResolverHelper
import com.example.ui.theme.IndigoPrimary

/**
 * Selector horizontal interactivo para insertar etiquetas dinámicas del sistema ({HORA}, {BATERIA}, etc.)
 * con un solo toque, evitando que el usuario tenga que memorizarlas.
 */
@Composable
fun VariablePickerChips(
    onInsertVariable: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Insertar datos dinámicos:"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VariableResolverHelper.AVAILABLE_VARIABLES.forEach { variable ->
                VariableChip(
                    variable = variable,
                    onClick = { onInsertVariable(variable.tag) }
                )
            }
        }
    }
}

@Composable
private fun VariableChip(
    variable: SystemVariable,
    onClick: () -> Unit
) {
    val icon = when (variable.iconName) {
        "schedule" -> Icons.Default.Schedule
        "calendar_today" -> Icons.Default.CalendarToday
        "event" -> Icons.Default.Event
        "battery_charging_full" -> Icons.Default.BatteryChargingFull
        "power" -> Icons.Default.Power
        "content_paste" -> Icons.Default.ContentPaste
        "smartphone" -> Icons.Default.Smartphone
        "notifications" -> Icons.Default.Notifications
        "notifications_active" -> Icons.Default.NotificationsActive
        else -> Icons.Default.Tag
    }

    AssistChip(
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = variable.tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndigoPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${variable.previewValue})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(14.dp)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Insertar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = IndigoPrimary.copy(alpha = 0.08f),
            labelColor = IndigoPrimary
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = IndigoPrimary.copy(alpha = 0.3f)
        ),
        modifier = Modifier.testTag("variable_chip_${variable.tag.replace("{", "").replace("}", "").lowercase()}")
    )
}
