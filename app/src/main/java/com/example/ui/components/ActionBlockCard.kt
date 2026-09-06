package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.ui.components.editors.BrightnessBlockEditor
import com.example.ui.components.editors.LuaScriptBlockEditor
import com.example.ui.components.editors.NotificationBlockEditor
import com.example.ui.components.editors.SpeakBlockEditor
import com.example.ui.components.editors.UserInteractionBlockEditor
import com.example.ui.components.editors.VolumeBlockEditor

@Composable
fun ActionBlockCard(
    block: ActionBlock,
    index: Int,
    totalBlocks: Int,
    blockColor: Color,
    context: Context,
    onBlockChange: (ActionBlock) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onRequestAppPicker: () -> Unit
) {
    var typeDropdownOpen by remember { mutableStateOf(false) }
    val currentActionType = remember(block.actionType) {
        ActionType.values().firstOrNull { it.name == block.actionType } ?: ActionType.OPEN_URL
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = blockColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior del bloque: Número de paso, reordenar y eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(blockColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PASO ${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentActionType.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index > 0) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Subir paso",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < totalBlocks - 1) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Bajar paso",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (totalBlocks > 1) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Eliminar paso",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de Tipo de Acción
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { typeDropdownOpen = true }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tipo de Acción",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = currentActionType.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(
                    expanded = typeDropdownOpen,
                    onDismissRequest = { typeDropdownOpen = false }
                ) {
                    ActionType.values().forEach { actionType ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = actionType.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (actionType == currentActionType) FontWeight.Bold else FontWeight.Normal,
                                            color = if (actionType == currentActionType) blockColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = actionType.paramLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        ),
                                        maxLines = 1
                                    )
                                }
                            },
                            onClick = {
                                val newParam = if (actionType.defaultParam.isNotEmpty()) actionType.defaultParam else ""
                                onBlockChange(
                                    block.copy(
                                        actionType = actionType.name,
                                        parameter = newParam,
                                        customLabel = actionType.label
                                    )
                                )
                                typeDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Editores especializados según el tipo de acción
            when (currentActionType) {
                ActionType.FLASHLIGHT, ActionType.SOUND_SETTINGS -> {
                    // No requieren parámetros
                }
                ActionType.OPEN_APP -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    val selectedPkg = block.parameter.trim()
                    val pm = context.packageManager
                    val appLabel = remember(selectedPkg) {
                        if (selectedPkg.isEmpty()) null
                        else try {
                            val appInfo = pm.getApplicationInfo(selectedPkg, 0)
                            pm.getApplicationLabel(appInfo).toString()
                        } catch (_: Exception) {
                            selectedPkg
                        }
                    }

                    OutlinedButton(
                        onClick = onRequestAppPicker,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (appLabel != null) "Aplicación: $appLabel" else "Seleccionar Aplicación...",
                            maxLines = 1
                        )
                    }
                }
                ActionType.SET_VOLUME -> {
                    VolumeBlockEditor(
                        parameter = block.parameter,
                        blockColor = blockColor,
                        onParameterChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        }
                    )
                }
                ActionType.SET_BRIGHTNESS -> {
                    BrightnessBlockEditor(
                        parameter = block.parameter,
                        blockColor = blockColor,
                        onParameterChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        }
                    )
                }
                ActionType.NOTIFICATION -> {
                    NotificationBlockEditor(
                        parameter = block.parameter,
                        blockColor = blockColor,
                        context = context,
                        onParameterChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        }
                    )
                }
                ActionType.SPEAK -> {
                    SpeakBlockEditor(
                        parameter = block.parameter,
                        blockColor = blockColor,
                        onParameterChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        }
                    )
                }
                ActionType.LUA_SCRIPT -> {
                    LuaScriptBlockEditor(
                        parameter = block.parameter,
                        onParameterChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        }
                    )
                }
                ActionType.USER_INTERACTION -> {
                    UserInteractionBlockEditor(
                        parameter = block.parameter,
                        blockColor = blockColor,
                        onParameterChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        }
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = block.parameter,
                        onValueChange = { newParam ->
                            onBlockChange(block.copy(parameter = newParam))
                        },
                        label = { Text(currentActionType.paramLabel, fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
