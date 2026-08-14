package com.example.ui.components.actioninputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortcutAction
import com.example.ui.components.VariablePickerChips
import com.example.ui.theme.IndigoPrimary

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

        // Botón de acción rápida: Leer notificación directamente
        OutlinedButton(
            onClick = {
                onUpdate(action.copy(param1 = "{ULTIMA_NOTIFICACION}", param2 = "read_notification"))
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = IndigoPrimary.copy(alpha = 0.05f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tts_use_notification_button")
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Leer texto de Notificación previa ({ULTIMA_NOTIFICACION})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = IndigoPrimary
            )
        }

        VariablePickerChips(
            onInsertVariable = { tag ->
                val current = action.param1
                val updated = if (current.isBlank()) tag else "$current $tag"
                onUpdate(action.copy(param1 = updated))
            }
        )
    }
}

