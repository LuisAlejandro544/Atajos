package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ShortcutAction
import com.example.ui.components.VariablePickerChips

@Composable
fun TtsInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = action.param1,
            onValueChange = { onUpdate(action.copy(param1 = it)) },
            label = { Text("Texto para leer en voz alta") },
            placeholder = { Text("Ej: ¡Hola! Son las {HORA} y tienes {BATERIA} de batería.") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tts_input_field"),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        VariablePickerChips(
            onInsertVariable = { tag ->
                val current = action.param1
                val updated = if (current.isBlank()) tag else "$current $tag"
                onUpdate(action.copy(param1 = updated))
            }
        )
    }
}
