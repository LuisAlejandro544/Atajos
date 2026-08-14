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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HttpRequestInputSection(
    action: ShortcutAction,
    onUpdate: (ShortcutAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMethod = action.param2.uppercase().ifBlank { "GET" }
    val currentUrl = action.param1
    val currentBody = action.param3

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Método HTTP:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("GET", "POST", "PUT", "DELETE").forEach { method ->
                FilterChip(
                    selected = currentMethod == method,
                    onClick = {
                        onUpdate(action.copy(param2 = method))
                    },
                    label = { Text(method, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    leadingIcon = {
                        if (method == "POST" || method == "PUT") {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        OutlinedTextField(
            value = currentUrl,
            onValueChange = { onUpdate(action.copy(param1 = it)) },
            label = { Text("URL de la API o Webhook") },
            placeholder = { Text("https://api.ejemplo.com/datos o webhook...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("http_url_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Selector de variables dinámicas para insertar en la URL o cuerpo
        VariablePickerChips(
            onInsertVariable = { tag ->
                if (currentMethod in listOf("POST", "PUT", "PATCH") && currentBody.isNotEmpty()) {
                    onUpdate(action.copy(param3 = "$currentBody$tag"))
                } else {
                    onUpdate(action.copy(param1 = "$currentUrl$tag"))
                }
            }
        )

        if (currentMethod in listOf("POST", "PUT", "PATCH")) {
            OutlinedTextField(
                value = currentBody,
                onValueChange = { onUpdate(action.copy(param3 = it)) },
                label = { Text("Cuerpo de la petición (JSON / Texto)") },
                placeholder = { Text("{\"mensaje\": \"Hola desde Atajos\", \"bateria\": \"{BATERIA}\"}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("http_body_input"),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Info de cómo encadenar la respuesta
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "El contenido devuelto estará disponible para los siguientes pasos usando {RESPUESTA_WEB} y {HTTP_STATUS} (por ej. para leerlo por voz o notificarlo).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
