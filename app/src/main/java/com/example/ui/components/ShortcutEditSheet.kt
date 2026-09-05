package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity
import com.example.executor.NotificationHelper
import com.example.executor.VariableResolver
import java.util.UUID

data class InstalledAppItem(
    val name: String,
    val packageName: String
)

@Composable
fun AppPickerDialog(
    apps: List<InstalledAppItem>,
    onSelectApp: (InstalledAppItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            val q = searchQuery.trim().lowercase()
            apps.filter { it.name.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Seleccionar App",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${filteredApps.size} apps encontradas",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar aplicación o paquete...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron aplicaciones instaladas",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectApp(app) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = app.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    val context = LocalContext.current
    val installedApps = remember(context) {
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val launcherApps = pm.queryIntentActivities(mainIntent, 0).mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString().trim()
                if (label.isNotBlank()) InstalledAppItem(name = label, packageName = pkg) else null
            }
            val allPackages = pm.getInstalledApplications(0).mapNotNull { appInfo ->
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    val label = pm.getApplicationLabel(appInfo).toString().trim()
                    if (label.isNotBlank()) InstalledAppItem(name = label, packageName = appInfo.packageName) else null
                } else null
            }
            (launcherApps + allPackages)
                .distinctBy { it.packageName }
                .sortedBy { it.name.lowercase() }
        } catch (_: Exception) {
            emptyList<InstalledAppItem>()
        }
    }
    var appPickerTargetIndex by remember { mutableStateOf<Int?>(null) }

    if (appPickerTargetIndex != null) {
        val targetIdx = appPickerTargetIndex!!
        AppPickerDialog(
            apps = installedApps,
            onSelectApp = { selectedApp ->
                if (targetIdx in actionBlocks.indices) {
                    val current = actionBlocks[targetIdx]
                    actionBlocks[targetIdx] = current.copy(
                        parameter = selectedApp.packageName,
                        customLabel = if (current.customLabel.isBlank() || current.customLabel.startsWith("Abrir")) {
                            "Abrir ${selectedApp.name}"
                        } else {
                            current.customLabel
                        }
                    )
                }
                appPickerTargetIndex = null
            },
            onDismiss = { appPickerTargetIndex = null }
        )
    }

    ModalBottomSheet(
        onDismissRequest = { /* No cerrar por arrastre o toque accidental; solo con el botón X */ },
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("shortcut_edit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // Header con botón X para cerrar y botón Guardar superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar editor",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (initialShortcut == null) "Nuevo Atajo" else "Editar Atajo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

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
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("top_save_shortcut_button")
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            val hasCustomWait = actionBlocks.any { it.actionType == ActionType.WAIT.name }
                            val waitBadgeText = if (hasCustomWait) "${actionBlocks.size} pasos (con espera personalizada)" else "${actionBlocks.size} pasos (espera 1s 3ms)"
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

            // Shortcut Name
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

            // Subtitle / Description
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

            // Color selection (ARRIBA)
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

            // Icon selection (ARRIBA)
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

            // Category selector (ARRIBA)
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

            // Favorite toggle (ARRIBA)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { isFavorite = !isFavorite }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Marcar como Favorito",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Aparece destacado en la pestaña de favoritos",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

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

                // Asignar color temático al bloque estilo Apple Shortcuts
                val blockColor = when (currentActionType) {
                    ActionType.FLASHLIGHT -> Color(0xFFFF9500)
                    ActionType.OPEN_APP -> Color(0xFF5856D6)
                    ActionType.OPEN_URL -> Color(0xFF007AFF)
                    ActionType.SET_VOLUME -> Color(0xFF34C759)
                    ActionType.COPY_TEXT -> Color(0xFF30B0C7)
                    ActionType.MAP_NAV -> Color(0xFF00C7BE)
                    ActionType.SET_TIMER -> Color(0xFFFF2D55)
                    ActionType.SEND_MESSAGE -> Color(0xFF0A84FF)
                    ActionType.SOUND_SETTINGS -> Color(0xFFBF5AF2)
                    ActionType.SHARE_TEXT -> Color(0xFF32D74B)
                    ActionType.SPEAK -> Color(0xFFFF375F)
                    ActionType.NOTIFICATION -> Color(0xFFFF9500)
                    ActionType.SET_BRIGHTNESS -> Color(0xFFFFCC00)
                    ActionType.WAIT -> Color(0xFFFF9F0A)
                    ActionType.LUA_SCRIPT -> Color(0xFFAF52DE)
                }

                var isPickerExpanded by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 1.dp,
                            color = blockColor.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("action_block_card_$index"),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header del bloque con badge estilizado, título y botones de control limpios
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(blockColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PASO ${index + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = blockColor,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = currentActionType.label,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (index > 0) {
                                    Surface(
                                        onClick = {
                                            val item = actionBlocks.removeAt(index)
                                            actionBlocks.add(index - 1, item)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowUpward,
                                                contentDescription = "Subir paso",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                if (index < actionBlocks.lastIndex) {
                                    Surface(
                                        onClick = {
                                            val item = actionBlocks.removeAt(index)
                                            actionBlocks.add(index + 1, item)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowDownward,
                                                contentDescription = "Bajar paso",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                if (actionBlocks.size > 1) {
                                    Surface(
                                        onClick = { actionBlocks.removeAt(index) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Eliminar paso",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selector elegante de acción estilo Píldora / Dropdown iOS
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isPickerExpanded = true }
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(blockColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = currentActionType.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Cambiar tipo de acción",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isPickerExpanded,
                                onDismissRequest = { isPickerExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                ActionType.values().forEach { act ->
                                    val isSelected = currentActionType == act
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = act.label,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            isPickerExpanded = false
                                            actionBlocks[index] = block.copy(
                                                actionType = act.name,
                                                parameter = if (block.parameter.isBlank()) act.defaultParam else block.parameter
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        when (currentActionType) {
                            ActionType.FLASHLIGHT, ActionType.SOUND_SETTINGS -> {
                                // No requieren parámetros adicionales
                            }
                            ActionType.OPEN_APP -> {
                                val selectedApp = installedApps.firstOrNull { it.packageName == block.parameter }
                                val displayName = selectedApp?.name ?: if (block.parameter.isNotBlank()) block.parameter else "Toca para elegir app..."

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { appPickerTargetIndex = index }
                                        .border(1.dp, blockColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(blockColor.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Apps, contentDescription = null, tint = blockColor, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (block.parameter.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                if (block.parameter.isNotBlank()) {
                                                    Text(
                                                        text = block.parameter,
                                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = { appPickerTargetIndex = index },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = blockColor),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(if (block.parameter.isBlank()) "Elegir" else "Cambiar", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                            ActionType.SET_VOLUME -> {
                                val currentVolume = block.parameter.trim().removeSuffix("%").toIntOrNull()?.coerceIn(0, 100) ?: 70
                                val volIcon = when {
                                    currentVolume == 0 -> Icons.Filled.VolumeMute
                                    currentVolume <= 40 -> Icons.Filled.VolumeDown
                                    else -> Icons.Filled.VolumeUp
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, blockColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(volIcon, contentDescription = null, tint = blockColor, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Nivel de volumen",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(blockColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "$currentVolume%",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = blockColor
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Slider(
                                            value = currentVolume.toFloat(),
                                            onValueChange = { newVal ->
                                                actionBlocks[index] = block.copy(parameter = newVal.roundToInt().toString())
                                            },
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = blockColor,
                                                activeTrackColor = blockColor
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("0% (Silencio)", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                            Text("50%", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                            Text("100% (Máx)", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Presets rápidos para un toque rápido en teléfono móvil
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(0 to "Mute", 30 to "30%", 70 to "70%", 100 to "100%").forEach { (presetVal, label) ->
                                                val isPresetActive = currentVolume == presetVal
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            actionBlocks[index] = block.copy(parameter = presetVal.toString())
                                                        }
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isPresetActive) blockColor else MaterialTheme.colorScheme.outlineVariant,
                                                            shape = RoundedCornerShape(8.dp)
                                                        ),
                                                    color = if (isPresetActive) blockColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isPresetActive) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontSize = 11.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ActionType.SET_BRIGHTNESS -> {
                                val currentBrightness = block.parameter.trim().removeSuffix("%").toIntOrNull()?.coerceIn(0, 100) ?: 80
                                val brightnessIcon = when {
                                    currentBrightness <= 25 -> Icons.Filled.BrightnessLow
                                    currentBrightness <= 70 -> Icons.Filled.BrightnessMedium
                                    else -> Icons.Filled.BrightnessHigh
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, blockColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(brightnessIcon, contentDescription = null, tint = blockColor, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Nivel de brillo de pantalla",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(blockColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "$currentBrightness%",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = blockColor
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Slider manual táctil
                                        Slider(
                                            value = currentBrightness.toFloat(),
                                            onValueChange = { newVal ->
                                                actionBlocks[index] = block.copy(parameter = newVal.roundToInt().toString())
                                            },
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = blockColor,
                                                activeTrackColor = blockColor
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("0% (Mínimo)", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                            Text("50%", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                            Text("100% (Máximo)", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Presets rápidos para toque con un solo dedo
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(20 to "20%", 50 to "50%", 80 to "80%", 100 to "100%").forEach { (presetVal, label) ->
                                                val isPresetActive = currentBrightness == presetVal
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            actionBlocks[index] = block.copy(parameter = presetVal.toString())
                                                        }
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isPresetActive) blockColor else MaterialTheme.colorScheme.outlineVariant,
                                                            shape = RoundedCornerShape(8.dp)
                                                        ),
                                                    color = if (isPresetActive) blockColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isPresetActive) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                fontSize = 11.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ActionType.NOTIFICATION -> {
                                Spacer(modifier = Modifier.height(10.dp))

                                val rawParam = block.parameter
                                val currentSound = if (rawParam.startsWith("sound:pop|")) NotificationHelper.SOUND_POP else NotificationHelper.SOUND_DEFAULT
                                val textContent = when {
                                    rawParam.startsWith("sound:pop|") -> rawParam.removePrefix("sound:pop|")
                                    rawParam.startsWith("sound:default|") -> rawParam.removePrefix("sound:default|")
                                    else -> rawParam
                                }

                                // Selector de sonido de notificación
                                Text(
                                    text = "Sonido de la Notificación:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Opción 1: Original del móvil (Sistema)
                                    val isDefaultSelected = currentSound == NotificationHelper.SOUND_DEFAULT
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val newParam = "sound:default|$textContent"
                                                actionBlocks[index] = block.copy(parameter = newParam)
                                                NotificationHelper.previewSound(context, NotificationHelper.SOUND_DEFAULT)
                                            }
                                            .border(
                                                width = if (isDefaultSelected) 2.dp else 1.dp,
                                                color = if (isDefaultSelected) blockColor else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        color = if (isDefaultSelected) blockColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Notifications,
                                                contentDescription = "Sonido original",
                                                tint = if (isDefaultSelected) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Original",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isDefaultSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isDefaultSelected) blockColor else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                                Text(
                                                    text = "Del móvil",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Opción 2: Pop Notification (GabrielAraujo CC0)
                                    val isPopSelected = currentSound == NotificationHelper.SOUND_POP
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val newParam = "sound:pop|$textContent"
                                                actionBlocks[index] = block.copy(parameter = newParam)
                                                NotificationHelper.previewSound(context, NotificationHelper.SOUND_POP)
                                            }
                                            .border(
                                                width = if (isPopSelected) 2.dp else 1.dp,
                                                color = if (isPopSelected) blockColor else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        color = if (isPopSelected) blockColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.VolumeUp,
                                                contentDescription = "Pop Notification",
                                                tint = if (isPopSelected) blockColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Pop Audio",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isPopSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isPopSelected) blockColor else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                                Text(
                                                    text = "GabrielAraujo (CC0)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Botón de escuchar muestra
                                    IconButton(
                                        onClick = {
                                            NotificationHelper.previewSound(context, currentSound)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(blockColor.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Escuchar sonido",
                                            tint = blockColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = textContent,
                                    onValueChange = { newText ->
                                        val prefix = if (currentSound == NotificationHelper.SOUND_POP) "sound:pop|" else "sound:default|"
                                        actionBlocks[index] = block.copy(parameter = "$prefix$newText")
                                    },
                                    label = { Text("Texto de la notificación", fontSize = 12.sp) },
                                    minLines = 2,
                                    maxLines = 4,
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
                                                    val newText = "$textContent$separator${v.key}"
                                                    val prefix = if (currentSound == NotificationHelper.SOUND_POP) "sound:pop|" else "sound:default|"
                                                    actionBlocks[index] = block.copy(parameter = "$prefix$newText")
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
                            ActionType.SPEAK -> {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = block.parameter,
                                    onValueChange = { newParam ->
                                        actionBlocks[index] = block.copy(parameter = newParam)
                                    },
                                    label = { Text("Texto que leerá el motor de voz", fontSize = 12.sp) },
                                    minLines = 2,
                                    maxLines = 4,
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
                                                    val current = block.parameter
                                                    val separator = if (current.isNotBlank() && !current.endsWith(" ")) " " else ""
                                                    actionBlocks[index] = block.copy(parameter = "$current$separator${v.key}")
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
                            else -> {
                                Spacer(modifier = Modifier.height(10.dp))
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
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = if (isLua) androidx.compose.ui.text.TextStyle(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 12.sp
                                    ) else MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

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
