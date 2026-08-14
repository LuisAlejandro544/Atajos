package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

@Composable
fun UtilitiesInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (action.type) {
        ActionType.SET_TIMER -> {
            Row(
                modifier = modifier.fillMaxWidth(),
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
                modifier = modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        ActionType.QUICK_CALCULATOR -> {
            Row(
                modifier = modifier.fillMaxWidth(),
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

        else -> Unit
    }
}
