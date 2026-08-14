package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.ui.components.ActionCard
import com.example.ui.components.AddActionBottomSheet
import com.example.ui.components.editor.AddNextActionButton
import com.example.ui.components.editor.ShortcutActionsEmptyState
import com.example.ui.components.editor.ShortcutActionsHeader
import com.example.ui.components.editor.ShortcutLivePreviewCard
import com.example.ui.components.editor.ShortcutMetadataSection
import com.example.ui.viewmodel.EditorState

/**
 * Pantalla principal del Editor de Atajos.
 * Orquesta la previsualización, metadatos y lista de acciones de forma modular.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutEditorScreen(
    state: EditorState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAddAction: (ActionType) -> Unit,
    onUpdateAction: (Int, ShortcutAction) -> Unit,
    onRemoveAction: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onSave: () -> Unit,
    onTestRun: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showActionPicker by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("Utilidades", "Productividad", "Comunicación", "Sistema", "Navegación", "General")
    }

    val activeColor = try {
        Color(android.graphics.Color.parseColor(state.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isNew) "Nuevo Atajo" else "Editar Atajo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onTestRun,
                        modifier = Modifier.testTag("editor_test_run_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Probar atajo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Preview Card
            item {
                ShortcutLivePreviewCard(
                    state = state,
                    activeColor = activeColor
                )
            }

            // Metadatos (Título, Descripción, Categoría, Color, Icono)
            item {
                ShortcutMetadataSection(
                    state = state,
                    categories = categories,
                    onTitleChange = onTitleChange,
                    onDescriptionChange = onDescriptionChange,
                    onCategoryChange = onCategoryChange,
                    onColorChange = onColorChange,
                    onIconChange = onIconChange
                )
            }

            // Pipeline de Acciones Header
            item {
                ShortcutActionsHeader(
                    actionCount = state.actions.size,
                    onAddClick = { showActionPicker = true }
                )
            }

            // Pipeline de Acciones Items o Estado Vacío
            if (state.actions.isEmpty()) {
                item {
                    ShortcutActionsEmptyState(
                        onAddClick = { showActionPicker = true }
                    )
                }
            } else {
                itemsIndexed(state.actions) { index, action ->
                    ActionCard(
                        stepIndex = index,
                        action = action,
                        totalSteps = state.actions.size,
                        onUpdate = { updated -> onUpdateAction(index, updated) },
                        onRemove = { onRemoveAction(index) },
                        onMoveUp = { onMoveUp(index) },
                        onMoveDown = { onMoveDown(index) }
                    )
                }

                item {
                    AddNextActionButton(
                        onClick = { showActionPicker = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showActionPicker) {
        AddActionBottomSheet(
            onDismiss = { showActionPicker = false },
            onSelectAction = { actionType ->
                onAddAction(actionType)
            }
        )
    }
}
