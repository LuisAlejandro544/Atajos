package com.example.ui.navigation

import com.example.data.model.AutomationEntity
import com.example.data.model.ShortcutEntity

/**
 * Contratos y agrupadores de eventos para la navegación y pantallas principales.
 */
data class HomeTabActions(
    val onCategorySelected: (String) -> Unit,
    val onSearchQueryChange: (String) -> Unit,
    val onRunShortcut: (ShortcutEntity) -> Unit,
    val onEditShortcut: (ShortcutEntity) -> Unit,
    val onDuplicateShortcut: (ShortcutEntity) -> Unit,
    val onToggleFavorite: (ShortcutEntity) -> Unit,
    val onDeleteShortcut: (Long) -> Unit,
    val onCreateNewShortcut: () -> Unit
)

data class AutomationsTabActions(
    val onToggleAutomation: (Long, Boolean) -> Unit,
    val onDeleteAutomation: (Long) -> Unit,
    val onTestRunAutomation: (AutomationEntity) -> Unit,
    val onOpenNewDialog: () -> Unit,
    val onCloseDialog: () -> Unit,
    val onSaveAutomation: (AutomationEntity) -> Unit
)

data class GalleryTabActions(
    val onInstallTemplate: (ShortcutEntity) -> Unit,
    val onTestRunTemplate: (ShortcutEntity) -> Unit
)

data class HistoryTabActions(
    val onClearHistory: () -> Unit
)

data class TopBarActions(
    val onRequestPermissions: () -> Unit,
    val onDismissPermissionBanner: () -> Unit,
    val onDismissExecutionStatus: () -> Unit
)

/**
 * Paquete unificado de callbacks para la navegación de pestañas.
 */
data class MainTabActions(
    val home: HomeTabActions,
    val automations: AutomationsTabActions,
    val gallery: GalleryTabActions,
    val history: HistoryTabActions,
    val topBar: TopBarActions
)
