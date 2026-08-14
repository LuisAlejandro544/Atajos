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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutAction
import com.example.engine.handlers.VibrationActionHandler
import com.example.ui.theme.IndigoPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VibrationInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vibrationHandler = remember { VibrationActionHandler(context) }

    val currentPattern = action.param2.ifBlank { "heavy" }

    val patterns = listOf(
        "heavy" to "Pulso Fuerte",
        "click" to "Toque Háptico",
        "double" to "Doble Toque",
        "heartbeat" to "Latido",
        "alert" to "Ráfaga Alerta",
        "sos" to "Código SOS",
        "custom" to "Personalizado"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Estilo de Vibración:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            patterns.forEach { (patternKey, label) ->
                FilterChip(
                    selected = currentPattern == patternKey,
                    onClick = {
                        onUpdate(action.copy(param2 = patternKey))
                        // Feedback instantáneo al seleccionar
                        scope.launch {
                            vibrationHandler.execute(action.copy(param2 = patternKey))
                        }
                    },
                    label = { Text(label, fontSize = 12.sp) },
                    leadingIcon = if (currentPattern == patternKey) {
                        {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = IndigoPrimary
                            )
                        }
                    } else null,
                    modifier = Modifier.testTag("vibration_chip_$patternKey")
                )
            }
        }

        if (currentPattern == "custom") {
            OutlinedTextField(
                value = action.param1.ifBlank { "300" },
                onValueChange = { onUpdate(action.copy(param1 = it)) },
                label = { Text("Duración de vibración (milisegundos)") },
                placeholder = { Text("300") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Botón para probar la vibración en vivo
        ElevatedButton(
            onClick = {
                scope.launch {
                    vibrationHandler.execute(action)
                }
            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = IndigoPrimary.copy(alpha = 0.12f),
                contentColor = IndigoPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("test_vibrate_button")
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Probar vibración ahora",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}
