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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.InstalledAppItem
import com.example.data.model.ShortcutEntity
import com.example.ui.components.dialogs.AppPickerDialog

@Composable
fun ShortcutEditSheet(
    initialShortcut: ShortcutEntity? = null,
    installedApps: List<InstalledAppItem> = emptyList(),
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
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialShortcut?.title ?: "") }
    var description by remember { mutableStateOf(initialShortcut?.description ?: "") }
    var selectedColor by remember { mutableStateOf(initialShortcut?.colorHex ?: "#007AFF") }
    var selectedIconKey by remember { mutableStateOf(initialShortcut?.iconKey ?: "FLASH") }
    var category by remember { mutableStateOf(initialShortcut?.category ?: "Utilidades") }
    var isFavorite by remember { mutableStateOf(initialShortcut?.isFavorite ?: false) }

    val actionBlocks = remember {
        mutableStateListOf<ActionBlock>().apply {
            if (initialShortcut != null && initialShortcut.actions.isNotEmpty()) {
                addAll(initialShortcut.actions)
            } else if (initialShortcut != null) {
                add(
                    ActionBlock(
                        actionType = initialShortcut.actionType,
                        parameter = initialShortcut.parameter,
                        customLabel = initialShortcut.title
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

    var showAppPickerForBlockIndex by remember { mutableStateOf<Int?>(null) }

    if (showAppPickerForBlockIndex != null) {
        val targetIndex = showAppPickerForBlockIndex!!
        AppPickerDialog(
            appsList = installedApps,
            onSelectApp = { selectedApp ->
                if (targetIndex in actionBlocks.indices) {
                    val oldBlock = actionBlocks[targetIndex]
                    actionBlocks[targetIndex] = oldBlock.copy(
                        parameter = selectedApp.packageName,
                        customLabel = "Abrir ${selectedApp.name}"
                    )
                }
                showAppPickerForBlockIndex = null
            },
            onDismiss = { showAppPickerForBlockIndex = null }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header con botón X explícito
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialShortcut == null) "Nuevo Atajo Multi-Bloque" else "Editar Atajo",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_edit_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta de Previsualización (Preview)
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
                            val hasCustomWait = actionBlocks.any { it.actionType == ActionType.WAIT.name }
                            val waitBadgeText = if (hasCustomWait) "${actionBlocks.size} pasos (con espera personalizada)"
                            else "${actionBlocks.size} pasos (espera 1s 3ms)"
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = waitBadgeText,
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

            // Nombre y Descripción
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre del atajo") },
                placeholder = { Text("Ej: Modo Juego, Salida & Música") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("Ej: Ejecuta acciones en cadena") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_desc_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Color
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(color)
                            .clickable { selectedColor = hex }
                            .then(
                                if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(14.dp))
                                else Modifier.border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Ícono
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

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Categoría
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

            Spacer(modifier = Modifier.height(14.dp))

            // Switch de Favorito
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Marcar como Favorito",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Aparecerá destacado en la pantalla principal",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = previewColor,
                        checkedTrackColor = previewColor.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secuencia de Acciones Multi-Bloque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECUENCIA DE ACCIONES (${actionBlocks.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Se ejecutarán una tras otra en este orden",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        )
                    )
                }

                OutlinedButton(
                    onClick = {
                        actionBlocks.add(
                            ActionBlock(
                                actionType = ActionType.COPY_TEXT.name,
                                parameter = "Texto copiado",
                                customLabel = "Copiar texto"
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir Paso", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lista de Bloques
            actionBlocks.forEachIndexed { index, block ->
                val blockColor = parseColorSafe(selectedColor)

                ActionBlockCard(
                    block = block,
                    index = index,
                    totalBlocks = actionBlocks.size,
                    blockColor = blockColor,
                    context = context,
                    onBlockChange = { updatedBlock ->
                        actionBlocks[index] = updatedBlock
                    },
                    onMoveUp = {
                        val item = actionBlocks.removeAt(index)
                        actionBlocks.add(index - 1, item)
                    },
                    onMoveDown = {
                        val item = actionBlocks.removeAt(index)
                        actionBlocks.add(index + 1, item)
                    },
                    onDelete = {
                        actionBlocks.removeAt(index)
                    },
                    onRequestAppPicker = {
                        showAppPickerForBlockIndex = index
                    }
                )

                // Indicador visual de espera entre bloques
                if (index < actionBlocks.lastIndex) {
                    val nextBlock = actionBlocks[index + 1]
                    val isNextWait = nextBlock.actionType == ActionType.WAIT.name
                    val isCurrentWait = block.actionType == ActionType.WAIT.name

                    if (!isCurrentWait && !isNextWait) {
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
                                text = "Pausa estándar: 1 segundo con 3 ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
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
