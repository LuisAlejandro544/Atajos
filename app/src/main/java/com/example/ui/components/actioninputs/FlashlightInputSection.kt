package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.ShortcutAction

@Composable
fun FlashlightInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
