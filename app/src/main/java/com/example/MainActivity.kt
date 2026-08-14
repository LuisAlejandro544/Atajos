package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AutomationsTabActions
import com.example.ui.navigation.GalleryTabActions
import com.example.ui.navigation.HistoryTabActions
import com.example.ui.navigation.HomeTabActions
import com.example.ui.navigation.MainTabActions
import com.example.ui.navigation.MainTabContent
import com.example.ui.navigation.TopBarActions
import com.example.ui.screens.ShortcutEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShortcutsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ShortcutsViewModel = viewModel()

                LaunchedEffect(intent) {
                    handleShortcutIntent(intent, viewModel)
                }

                ShortcutsApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?, viewModel: ShortcutsViewModel) {
        if (intent?.action == "com.example.ACTION_RUN_SHORTCUT") {
            val shortcutId = intent.getLongExtra("EXTRA_SHORTCUT_ID", -1L)
            if (shortcutId != -1L) {
                viewModel.runShortcutById(shortcutId)
            }
        }
    }
}

@Composable
fun ShortcutsApp(
    viewModel: ShortcutsViewModel = viewModel()
) {
    val context = LocalContext.current

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

    // Manejo y Solicitud Dinámica de Permisos
    var showPermissionBanner by remember { mutableStateOf(false) }

    val permissionList = remember {
        val list = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list.add(Manifest.permission.CAMERA)
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val hasMissingPermission = results.values.any { !it }
        showPermissionBanner = hasMissingPermission
    }

    LaunchedEffect(Unit) {
        val missing = permissionList.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

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
        val mainTabActions = remember(viewModel) {
            MainTabActions(
                home = HomeTabActions(
                    onCategorySelected = viewModel::setSelectedCategory,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onRunShortcut = viewModel::runShortcut,
                    onEditShortcut = viewModel::openEditShortcut,
                    onDuplicateShortcut = viewModel::duplicateShortcut,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onDeleteShortcut = viewModel::deleteShortcut,
                    onCreateNewShortcut = viewModel::openNewShortcutEditor
                ),
                automations = AutomationsTabActions(
                    onToggleAutomation = viewModel::toggleAutomation,
                    onDeleteAutomation = viewModel::deleteAutomation,
                    onTestRunAutomation = viewModel::testRunAutomation,
                    onOpenNewDialog = viewModel::openNewAutomationDialog,
                    onCloseDialog = viewModel::closeAutomationDialog,
                    onSaveAutomation = viewModel::saveAutomation
                ),
                gallery = GalleryTabActions(
                    onInstallTemplate = viewModel::installGalleryTemplate,
                    onTestRunTemplate = viewModel::runShortcut
                ),
                history = HistoryTabActions(
                    onClearHistory = viewModel::clearLogs
                ),
                topBar = TopBarActions(
                    onRequestPermissions = { permissionLauncher.launch(permissionList) },
                    onDismissPermissionBanner = { showPermissionBanner = false },
                    onDismissExecutionStatus = viewModel::dismissExecutionStatus
                )
            )
        }

        MainTabContent(
            selectedTab = selectedTab,
            filteredShortcuts = filteredShortcuts,
            allShortcuts = allShortcuts,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            automations = automations,
            showAutomationDialog = showAutomationDialog,
            galleryTemplates = viewModel.galleryTemplates,
            recentLogs = recentLogs,
            executionStatus = executionStatus,
            showPermissionBanner = showPermissionBanner,
            onTabSelected = viewModel::setSelectedTab,
            actions = mainTabActions
        )
    }
}
