package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.InstalledAppItem
import com.example.data.model.ShortcutEntity
import com.example.data.model.TriggerType
import com.example.ui.components.dialogs.AppPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutEditSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
        isFavorite: Boolean,
        triggerType: String,
        backgroundImageUri: String?
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
    var triggerType by remember { mutableStateOf(initialShortcut?.triggerType ?: TriggerType.MANUAL.name) }
    var backgroundImageUri by remember { mutableStateOf(initialShortcut?.backgroundImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            backgroundImageUri = uri.toString()
        }
    }

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialShortcut == null) "Nuevo Atajo" else "Editar Atajo",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta de Previsualización (Preview) en tiempo real
            val previewColor = parseColorSafe(selectedColor)
            val previewGradient = Brush.verticalGradient(
                listOf(
                    previewColor,
                    previewColor.copy(
                        red = (previewColor.red * 0.75f).coerceIn(0f, 1f),
                        green = (previewColor.green * 0.75f).coerceIn(0f, 1f),
                        blue = (previewColor.blue * 0.75f).coerceIn(0f, 1f)
                    )
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(previewGradient)
            ) {
                if (!backgroundImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(backgroundImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.65f),
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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

                            if (triggerType == TriggerType.CHARGER_CONNECTED.name) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚡ Cargador",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFFD60A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            } else if (triggerType == TriggerType.CHARGER_DISCONNECTED.name) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🔋 Batería",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFF9500),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
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
                                if (actionBlocks.size > 1) "${actionBlocks.size} pasos encadenados" else "Ejecuta acción"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
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
                placeholder = { Text("Ej: Modo Juego, Carga & Mensaje") },
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
                placeholder = { Text("Ej: Ejecuta acciones al conectar cargador") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_desc_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Disparador Automático (Trigger)
            Text(
                text = "DISPARADOR DE INICIO (TRIGGER)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Permite que el atajo se inicie automáticamente ante un evento del sistema",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(TriggerType.MANUAL.name, "Manual", Icons.Filled.TouchApp),
                    Triple(TriggerType.CHARGER_CONNECTED.name, "⚡ Al conectar cargador", Icons.Filled.Bolt),
                    Triple(TriggerType.CHARGER_DISCONNECTED.name, "🔋 Al desconectar cargador", Icons.Filled.BatteryChargingFull)
                ).forEach { (typeKey, label, icon) ->
                    val isSelected = triggerType == typeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { triggerType = typeKey },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Foto de Fondo de la Tarjeta
            Text(
                text = "FOTO DE FONDO DE LA TARJETA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personaliza la tarjeta con cualquier imagen de tu galería",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (backgroundImageUri.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar foto de fondo")
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(backgroundImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Foto aplicada",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Fondo activo en tarjeta",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cambiar", fontSize = 12.sp)
                            }
                            IconButton(onClick = { backgroundImageUri = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Quitar foto",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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
                        isFavorite,
                        triggerType,
                        backgroundImageUri
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
