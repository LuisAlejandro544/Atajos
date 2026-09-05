package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.ui.components.ExecutionBanner
import com.example.ui.components.ShortcutCard
import com.example.ui.components.ShortcutEditSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutScreen(
    viewModel: ShortcutViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTopMenu by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Favoritos", "Utilidades", "Productividad", "Comunicación", "Viajes")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateSheet() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("create_shortcut_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Crear nuevo atajo",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Atajos",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.filteredShortcuts.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Toca cualquier tarjeta para ejecutar su acción",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.openCreateSheet() },
                            modifier = Modifier.testTag("top_add_shortcut_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Añadir atajo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box {
                            IconButton(onClick = { showTopMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menú",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Restaurar atajos iniciales") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Refresh, contentDescription = null)
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        viewModel.resetToDefaults()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Abrir Inspector Chucker (Red)") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.BugReport, contentDescription = null)
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        try {
                                            val intent = com.chuckerteam.chucker.api.Chucker.getLaunchIntent(context)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ver Fugas LeakCanary") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.BugReport, contentDescription = null)
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        try {
                                            val intent = android.content.Intent().apply {
                                                setClassName(context.packageName, "leakcanary.internal.activity.LeakActivity")
                                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Probar solicitud de Red (Chucker)") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Refresh, contentDescription = null)
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                            try {
                                                val req = okhttp3.Request.Builder()
                                                    .url("https://httpbin.org/get?app=shortcuts")
                                                    .build()
                                                com.example.NetworkClientProvider.okHttpClient.newCall(req).execute().close()
                                            } catch (_: Exception) {}
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar atajos...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .testTag("search_shortcuts_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categories Tab Row
                val selectedIndex = categories.indexOf(uiState.selectedCategory).coerceAtLeast(0)
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 20.dp,
                    indicator = { tabPositions ->
                        if (selectedIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { viewModel.selectCategory(category) },
                            text = {
                                Text(
                                    text = category,
                                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Shortcuts Grid or Empty State
                if (uiState.filteredShortcuts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inbox,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty())
                                    "No hay resultados para \"${uiState.searchQuery}\""
                                else "No hay atajos en esta categoría",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pulsa el botón + para crear un nuevo atajo personalizado o restaura los atajos predeterminados.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 10.dp,
                            bottom = 90.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("shortcuts_grid")
                    ) {
                        items(
                            items = uiState.filteredShortcuts,
                            key = { it.id }
                        ) { shortcut ->
                            ShortcutCard(
                                shortcut = shortcut,
                                isExecuting = uiState.executingShortcutId == shortcut.id,
                                onExecute = { viewModel.executeShortcut(shortcut) },
                                onEdit = { viewModel.openEditSheet(shortcut) },
                                onToggleFavorite = { viewModel.toggleFavorite(shortcut) },
                                onDelete = { viewModel.deleteShortcut(shortcut) }
                            )
                        }
                    }
                }
            }

            // Top Floating Execution Notification Banner (iOS Pill Style)
            ExecutionBanner(
                banner = uiState.banner,
                onDismiss = { viewModel.dismissBanner() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
        }

        // Bottom Sheet for Create / Edit
        if (uiState.isSheetOpen) {
            ShortcutEditSheet(
                sheetState = sheetState,
                initialShortcut = uiState.editingShortcut,
                onDismiss = { viewModel.closeSheet() },
                onSave = { title, description, colorHex, iconKey, actions, category, isFavorite ->
                    viewModel.saveShortcut(
                        title = title,
                        description = description,
                        colorHex = colorHex,
                        iconKey = iconKey,
                        actions = actions,
                        category = category,
                        isFavorite = isFavorite
                    )
                },
                onDelete = { shortcut ->
                    viewModel.deleteShortcut(shortcut)
                }
            )
        }
    }
}
