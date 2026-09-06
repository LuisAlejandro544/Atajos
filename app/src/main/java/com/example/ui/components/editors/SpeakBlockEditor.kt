package com.example.ui.components.editors

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.executor.VariableResolver
import com.example.executor.handlers.TtsManager
import com.example.piper.PiperTtsManager
import com.example.tts.TtsEngineType
import com.example.tts.TtsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun SpeakBlockEditor(
    parameter: String,
    blockColor: Color,
    context: Context = LocalContext.current,
    onParameterChange: (String) -> Unit
) {
    // Parsear parámetros estructurados JSON o plano
    var textContent by remember(parameter) {
        val parsedText = if (parameter.trim().startsWith("{") && parameter.trim().endsWith("}")) {
            try {
                JSONObject(parameter).optString("text", parameter)
            } catch (_: Exception) {
                parameter
            }
        } else {
            parameter
        }
        mutableStateOf(parsedText)
    }

    var selectedEngineId by remember(parameter) {
        val parsedEngine = if (parameter.trim().startsWith("{") && parameter.trim().endsWith("}")) {
            try {
                JSONObject(parameter).optString("engine", "DEFAULT")
            } catch (_: Exception) {
                "DEFAULT"
            }
        } else {
            "DEFAULT"
        }
        mutableStateOf(parsedEngine)
    }

    var selectedVoiceId by remember(parameter) {
        val parsedVoice = if (parameter.trim().startsWith("{") && parameter.trim().endsWith("}")) {
            try {
                JSONObject(parameter).optString("voice", "es_ES-carlfm-x_low")
            } catch (_: Exception) {
                "es_ES-carlfm-x_low"
            }
        } else {
            "es_ES-carlfm-x_low"
        }
        mutableStateOf(parsedVoice)
    }

    fun updateParam(newText: String, newEngineId: String, newVoiceId: String) {
        textContent = newText
        selectedEngineId = newEngineId
        selectedVoiceId = newVoiceId

        if (newEngineId == "DEFAULT") {
            // Guardar texto limpio compatible con versiones previas
            onParameterChange(newText)
        } else {
            val json = JSONObject().apply {
                put("text", newText)
                put("engine", newEngineId)
                put("voice", newVoiceId)
            }
            onParameterChange(json.toString())
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))

        // Selector de Motor de Voz para este bloque
        Text(
            text = "Motor de síntesis de voz:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedEngineId == "DEFAULT",
                onClick = { updateParam(textContent, "DEFAULT", selectedVoiceId) },
                label = { Text("Predeterminado", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = blockColor.copy(alpha = 0.2f),
                    selectedLabelColor = blockColor
                )
            )
            FilterChip(
                selected = selectedEngineId == "PIPER",
                onClick = { updateParam(textContent, "PIPER", selectedVoiceId) },
                label = { Text("Piper Neuronal (VITS)", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = blockColor.copy(alpha = 0.2f),
                    selectedLabelColor = blockColor
                )
            )
            FilterChip(
                selected = selectedEngineId == "ESPEAK",
                onClick = { updateParam(textContent, "ESPEAK", "es") },
                label = { Text("eSpeak-NG", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = blockColor.copy(alpha = 0.2f),
                    selectedLabelColor = blockColor
                )
            )
            FilterChip(
                selected = selectedEngineId == "SYSTEM",
                onClick = { updateParam(textContent, "SYSTEM", "") },
                label = { Text("Sistema Android", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = blockColor.copy(alpha = 0.2f),
                    selectedLabelColor = blockColor
                )
            )
        }

        // Si se seleccionó Piper, permitir elegir la voz neuronal
        if (selectedEngineId == "PIPER") {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Voz neuronal de Piper:",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PiperTtsManager.AVAILABLE_VOICES.forEach { v ->
                    val isAvail = PiperTtsManager.isVoiceAvailable(context, v.id)
                    FilterChip(
                        selected = selectedVoiceId == v.id,
                        onClick = { updateParam(textContent, selectedEngineId, v.id) },
                        label = {
                            Text(
                                text = "${v.name}${if (isAvail) "" else " (Descargar)"}",
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para la voz con botón de prueba rápida
        OutlinedTextField(
            value = textContent,
            onValueChange = { updateParam(it, selectedEngineId, selectedVoiceId) },
            label = { Text("Texto que leerá el motor de voz", fontSize = 12.sp) },
            minLines = 2,
            maxLines = 4,
            trailingIcon = {
                IconButton(
                    onClick = {
                        val ttsMgr = TtsManager(context)
                        val engineType = if (selectedEngineId != "DEFAULT") TtsEngineType.fromId(selectedEngineId) else null
                        val voice = if (selectedEngineId == "PIPER") selectedVoiceId else null
                        CoroutineScope(Dispatchers.IO).launch {
                            ttsMgr.speak(textContent, engineType, voice)
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Probar voz",
                        tint = blockColor
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Insertar variable dinámica:",
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VariableResolver.AVAILABLE_VARIABLES.forEach { v ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val separator = if (textContent.isNotBlank() && !textContent.endsWith(" ")) " " else ""
                            updateParam("$textContent$separator${v.key}", selectedEngineId, selectedVoiceId)
                        }
                        .border(1.dp, blockColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    color = blockColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+ ${v.key}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = blockColor,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${v.label})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
