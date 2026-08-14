package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ExecutionStatusBanner
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ShortcutEditorScreen
import com.example.ui.screens.ShortcutsHomeScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShortcutsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ShortcutsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsApp(
    viewModel: ShortcutsViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val filteredShortcuts by viewModel.filteredShortcuts.collectAsStateWithLifecycle()
    val allShortcuts by viewModel.allShortcuts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val automations by viewModel.allAutomations.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val executionStatus by viewModel.executionStatus.collectAsStateWithLifecycle()
    val showAutomationDialog by viewModel.showAutomationDialog.collectAsStateWithLifecycle()

    if (editorState.isEditing) {
        ShortcutEditorScreen(
            state = editorState,
            onTitleChange = viewModel::updateEditorTitle,
            onDescriptionChange = viewModel::updateEditorDescription,
            onColorChange = viewModel::updateEditorColor,
            onIconChange = viewModel::updateEditorIcon,
            onCategoryChange = viewModel::updateEditorCategory,
            onAddAction = viewModel::addActionToEditor,
            onUpdateAction = viewModel::updateActionInEditor,
            onRemoveAction = viewModel::removeActionFromEditor,
            onMoveUp = viewModel::moveActionUp,
            onMoveDown = viewModel::moveActionDown,
            onSave = viewModel::saveEditorShortcut,
            onTestRun = viewModel::testRunEditorShortcut,
            onClose = viewModel::closeEditor
        )
    } else {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Atajos",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    // Dynamic live execution status banner
                    ExecutionStatusBanner(
                        status = executionStatus,
                        onDismiss = viewModel::dismissExecutionStatus
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = { Icon(Icons.Default.Widgets, contentDescription = "Atajos") },
                        label = { Text("Atajos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_shortcuts")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = { Icon(Icons.Default.AutoMode, contentDescription = "Automatizaciones") },
                        label = { Text("Automatizar") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_automations")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Galería") },
                        label = { Text("Galería") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_gallery")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.setSelectedTab(3) },
                        icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                        label = { Text("Historial") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_history")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> ShortcutsHomeScreen(
                            shortcuts = filteredShortcuts,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            onCategorySelected = viewModel::setSelectedCategory,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onRunShortcut = viewModel::runShortcut,
                            onEditShortcut = viewModel::openEditShortcut,
                            onDuplicateShortcut = viewModel::duplicateShortcut,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onDeleteShortcut = viewModel::deleteShortcut,
                            onCreateNewShortcut = viewModel::openNewShortcutEditor
                        )

                        1 -> AutomationsScreen(
                            automations = automations,
                            availableShortcuts = allShortcuts,
                            showDialog = showAutomationDialog,
                            onToggleAutomation = viewModel::toggleAutomation,
                            onDeleteAutomation = viewModel::deleteAutomation,
                            onTestRunAutomation = viewModel::testRunAutomation,
                            onOpenNewDialog = viewModel::openNewAutomationDialog,
                            onCloseDialog = viewModel::closeAutomationDialog,
                            onSaveAutomation = viewModel::saveAutomation
                        )

                        2 -> GalleryScreen(
                            templates = viewModel.galleryTemplates,
                            onInstallTemplate = viewModel::installGalleryTemplate,
                            onTestRunTemplate = viewModel::runShortcut
                        )

                        3 -> HistoryScreen(
                            logs = recentLogs,
                            onClearHistory = viewModel::clearLogs
                        )
                    }
                }
            }
        }
    }
}
