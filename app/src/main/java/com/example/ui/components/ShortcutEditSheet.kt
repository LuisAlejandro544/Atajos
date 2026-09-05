package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutEditSheet(
    sheetState: SheetState,
    initialShortcut: ShortcutEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        colorHex: String,
        iconKey: String,
        actions: List<ActionBlock>,
        category: String,
        isFavorite: Boolean
    ) -> Unit,
    onDelete: ((ShortcutEntity) -> Unit)? = null
) {
    var title by remember(initialShortcut) {
        mutableStateOf(initialShortcut?.title ?: "")
    }
    var description by remember(initialShortcut) {
        mutableStateOf(initialShortcut?.description ?: "")
    }
    var selectedColor by remember(initialShortcut) {
        mutableStateOf(initialShortcut?.colorHex ?: "#007AFF")
    }
    var selectedIconKey by remember(initialShortcut) {
        mutableStateOf(initialShortcut?.iconKey ?: "FLASH")
    }
    var category by remember(initialShortcut) {
        mutableStateOf(initialShortcut?.category ?: "Utilidades")
    }
    var isFavorite by remember(initialShortcut) {
        mutableStateOf(initialShortcut?.isFavorite ?: false)
    }

    val actionBlocks = remember(initialShortcut) {
        mutableStateListOf<ActionBlock>().apply {
            if (initialShortcut != null && initialShortcut.actions.isNotEmpty()) {
                addAll(initialShortcut.actions)
            } else if (initialShortcut != null) {
                add(
                    ActionBlock(
                        actionType = initialShortcut.actionType,
                        parameter = initialShortcut.parameter,
                        customLabel = initialShortcut.description
                    )
                )
            } else {
                add(
                    ActionBlock(
                        actionType = ActionType.FLASHLIGHT.name,
                        parameter = "",
                        customLabel = "Alternar Linterna"
                    )
                )
            }
        }
    }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("shortcut_edit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialShortcut == null) "Nuevo Atajo Multi-Bloque" else "Editar Atajo",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preview Card
            Text(
                text = "VISTA PREVIA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            val previewColor = parseColorSafe(selectedColor)
            val previewGradient = Brush.linearGradient(
                listOf(
                    previewColor,
                    previewColor.copy(
                        red = (previewColor.red * 0.8f).coerceIn(0f, 1f),
                        green = (previewColor.green * 0.8f).coerceIn(0f, 1f),
                        blue = (previewColor.blue * 0.8f).coerceIn(0f, 1f)
                    )
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(previewGradient)
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ShortcutIconHelper.getIcon(selectedIconKey),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${actionBlocks.size} pasos (espera 1s 3ms)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Column {
                        Text(
                            text = title.ifEmpty { "Nombre de Atajo" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = description.ifEmpty {
                                if (actionBlocks.size > 1) "${actionBlocks.size} acciones encadenadas" else "Ejecuta acción"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Shortcut Name
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre del atajo") },
                placeholder = { Text("Ej: Modo Salida, Ruta & Música") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle / Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("Ej: Ejecuta acciones en cadena") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_desc_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Multi-block Action Sequence Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BLOQUES DE ACCIÓN (${actionBlocks.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Espera entre bloques: 1s y 3ms (1003 ms)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Button(
                    onClick = {
                        actionBlocks.add(
                            ActionBlock(
                                id = UUID.randomUUID().toString(),
                                actionType = ActionType.COPY_TEXT.name,
                                parameter = "Texto nuevo",
                                customLabel = "Acción adicional"
                            )
                        )
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_action_block_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir bloque", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Blocks List
            actionBlocks.forEachIndexed { index, block ->
                val currentActionType = try {
                    ActionType.valueOf(block.actionType)
                } catch (_: Exception) {
                    ActionType.OPEN_URL
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("action_block_card_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentActionType.label,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row {
                                if (index > 0) {
                                    IconButton(
                                        onClick = {
                                            val item = actionBlocks.removeAt(index)
                                            actionBlocks.add(index - 1, item)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Subir", modifier = Modifier.size(18.dp))
                                    }
                                }
                                if (index < actionBlocks.lastIndex) {
                                    IconButton(
                                        onClick = {
                                            val item = actionBlocks.removeAt(index)
                                            actionBlocks.add(index + 1, item)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Bajar", modifier = Modifier.size(18.dp))
                                    }
                                }
                                if (actionBlocks.size > 1) {
                                    IconButton(
                                        onClick = { actionBlocks.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Eliminar bloque",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Type selector for this block
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ActionType.values().forEach { act ->
                                FilterChip(
                                    selected = currentActionType == act,
                                    onClick = {
                                        actionBlocks[index] = block.copy(
                                            actionType = act.name,
                                            parameter = if (block.parameter.isBlank()) act.defaultParam else block.parameter
                                        )
                                    },
                                    label = { Text(act.label, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (currentActionType.paramLabel.isNotEmpty() &&
                            currentActionType != ActionType.FLASHLIGHT &&
                            currentActionType != ActionType.SOUND_SETTINGS
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val isLua = currentActionType == ActionType.LUA_SCRIPT
                            OutlinedTextField(
                                value = block.parameter,
                                onValueChange = { newParam ->
                                    actionBlocks[index] = block.copy(parameter = newParam)
                                },
                                label = { Text(currentActionType.paramLabel, fontSize = 12.sp) },
                                singleLine = !isLua,
                                minLines = if (isLua) 4 else 1,
                                maxLines = if (isLua) 10 else 1,
                                textStyle = if (isLua) androidx.compose.ui.text.TextStyle(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 12.sp
                                ) else MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Visual Indicator of the 1003 ms wait between blocks
                if (index < actionBlocks.lastIndex) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pausa de 1 segundo con 3 ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Color selection
            Text(
                text = "COLOR DE LA TARJETA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShortcutIconHelper.availableColors.forEach { (hex, name) ->
                    val color = parseColorSafe(hex)
                    val isSelected = selectedColor.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = hex }
                            .then(
                                if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                else Modifier
                            )
                            .testTag("color_choice_$hex"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = name,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Icon selection
            Text(
                text = "ÍCONO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShortcutIconHelper.availableIcons.forEach { iconItem ->
                    val isSelected = selectedIconKey == iconItem.key

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedIconKey = iconItem.key }
                            .testTag("icon_choice_${iconItem.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconItem.icon,
                            contentDescription = iconItem.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category selector
            Text(
                text = "CATEGORÍA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            val categories = listOf("Utilidades", "Productividad", "Comunicación", "Viajes", "Ajustes")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Favorite toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFavorite = !isFavorite }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Marcar como Favorito",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Aparecerá destacado en la pestaña de favoritos",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    onSave(
                        title.ifBlank { "Mi Atajo Multi-Bloque" },
                        description.ifBlank {
                            if (actionBlocks.size > 1) "${actionBlocks.size} acciones encadenadas" else "Acción de atajo"
                        },
                        selectedColor,
                        selectedIconKey,
                        actionBlocks.toList(),
                        category,
                        isFavorite
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_shortcut_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (initialShortcut == null) "Guardar Atajo (${actionBlocks.size} pasos)" else "Guardar Cambios",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (initialShortcut != null && onDelete != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onDelete(initialShortcut) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_shortcut_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar este Atajo")
                }
            }
        }
    }
}
