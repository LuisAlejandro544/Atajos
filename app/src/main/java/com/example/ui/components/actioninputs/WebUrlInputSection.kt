package com.example.ui.components.actioninputs

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
fun WebUrlInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (action.type == ActionType.OPEN_URL) {
        OutlinedTextField(
            value = action.param1,
            onValueChange = { onUpdate(action.copy(param1 = it)) },
            label = { Text("URL del sitio web") },
            placeholder = { Text("https://ejemplo.com") },
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        OutlinedTextField(
            value = action.param1,
            onValueChange = { onUpdate(action.copy(param1 = it)) },
            label = { Text("Término de búsqueda") },
            placeholder = { Text("Ej: el tiempo en Madrid") },
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
