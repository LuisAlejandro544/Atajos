package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutAction
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.IndigoPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrightnessInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSetting = action.param1.ifBlank { "50" }
    val sliderValue = currentSetting.toFloatOrNull() ?: 50f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Nivel de brillo o acción rápida:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Chips de opciones rápidas
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BrightnessChip(
                label = "Subir (+20%)",
                icon = Icons.Default.BrightnessHigh,
                selected = currentSetting == "increase",
                onClick = { onUpdate(action.copy(param1 = "increase")) }
            )
            BrightnessChip(
                label = "Bajar (-20%)",
                icon = Icons.Default.BrightnessLow,
                selected = currentSetting == "decrease",
                onClick = { onUpdate(action.copy(param1 = "decrease")) }
            )
            BrightnessChip(
                label = "Mínimo (10%)",
                icon = Icons.Default.BrightnessLow,
                selected = currentSetting == "10",
                onClick = { onUpdate(action.copy(param1 = "10")) }
            )
            BrightnessChip(
                label = "Medio (50%)",
                icon = Icons.Default.BrightnessMedium,
                selected = currentSetting == "50",
                onClick = { onUpdate(action.copy(param1 = "50")) }
            )
            BrightnessChip(
                label = "Máximo (100%)",
                icon = Icons.Default.BrightnessHigh,
                selected = currentSetting == "100",
                onClick = { onUpdate(action.copy(param1 = "100")) }
            )
        }

        // Slider de ajuste preciso de brillo porcentual
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brightness6,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Porcentaje exacto:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (currentSetting == "increase" || currentSetting == "decrease") {
                            if (currentSetting == "increase") "Aumentar +20%" else "Reducir -20%"
                        } else {
                            "${sliderValue.roundToInt()}%"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                }

                Slider(
                    value = sliderValue.coerceIn(0f, 100f),
                    onValueChange = { newValue ->
                        onUpdate(action.copy(param1 = newValue.roundToInt().toString()))
                    },
                    valueRange = 5f..100f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberAccent,
                        activeTrackColor = AmberAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brightness_slider")
                )
            }
        }
    }
}

@Composable
private fun BrightnessChip(
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
