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
fun NotificationInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = action.param1,
            onValueChange = { onUpdate(action.copy(param1 = it)) },
            label = { Text("Título de la notificación") },
            placeholder = { Text("Ej: Estado a las {HORA}") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notification_title_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = action.param2,
            onValueChange = { onUpdate(action.copy(param2 = it)) },
            label = { Text("Mensaje de la notificación") },
            placeholder = { Text("Ej: Batería al {BATERIA} ({ESTADO_BATERIA})") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notification_message_input"),
            shape = RoundedCornerShape(12.dp)
        )

        VariablePickerChips(
            title = "Insertar datos en el mensaje:",
            onInsertVariable = { tag ->
                val current = action.param2
                val updated = if (current.isBlank()) tag else "$current $tag"
                onUpdate(action.copy(param2 = updated))
            }
        )
    }
}
