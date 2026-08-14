package com.example.ui.components.actioninputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.ui.components.VariablePickerChips

@Composable
fun MessagingInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (action.type) {
        ActionType.COPY_CLIPBOARD -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = action.param1,
                    onValueChange = { onUpdate(action.copy(param1 = it)) },
                    label = { Text("Texto para copiar al portapapeles") },
                    placeholder = { Text("Texto que deseas guardar") },
                    modifier = Modifier.fillMaxWidth(),
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

        ActionType.SEND_WHATSAPP -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = action.param1,
                    onValueChange = { onUpdate(action.copy(param1 = it)) },
                    label = { Text("Número de teléfono (opcional)") },
                    placeholder = { Text("Ej: +34600000000 o vacío para elegir") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = action.param2,
                    onValueChange = { onUpdate(action.copy(param2 = it)) },
                    label = { Text("Mensaje de WhatsApp") },
                    placeholder = { Text("Mensaje predefinido") },
                    modifier = Modifier.fillMaxWidth(),
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

        ActionType.SEND_SMS -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = action.param1,
                    onValueChange = { onUpdate(action.copy(param1 = it)) },
                    label = { Text("Número de teléfono") },
                    placeholder = { Text("Número destinatario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = action.param2,
                    onValueChange = { onUpdate(action.copy(param2 = it)) },
                    label = { Text("Texto del SMS") },
                    placeholder = { Text("Mensaje SMS") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                VariablePickerChips(
                    title = "Insertar datos en el SMS:",
                    onInsertVariable = { tag ->
                        val current = action.param2
                        val updated = if (current.isBlank()) tag else "$current $tag"
                        onUpdate(action.copy(param2 = updated))
                    }
                )
            }
        }

        ActionType.SHARE_TEXT -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = action.param1,
                    onValueChange = { onUpdate(action.copy(param1 = it)) },
                    label = { Text("Texto a compartir") },
                    placeholder = { Text("Contenido para enviar a otras apps") },
                    modifier = Modifier.fillMaxWidth(),
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

        else -> Unit
    }
}
