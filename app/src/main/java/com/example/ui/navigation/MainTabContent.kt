package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.AutomationEntity
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutEntity
import com.example.engine.ExecutionStatus
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ShortcutsHomeScreen

/**
 * Contenedor principal de pestañas con Scaffold, TopBar, BottomBar y transiciones animadas.
 */
@Composable
fun MainTabContent(
    selectedTab: Int,
    filteredShortcuts: List<ShortcutEntity>,
    allShortcuts: List<ShortcutEntity>,
    selectedCategory: String,
    searchQuery: String,
    automations: List<AutomationEntity>,
    showAutomationDialog: Boolean,
    galleryTemplates: List<ShortcutEntity>,
    recentLogs: List<ExecutionLogEntity>,
    executionStatus: ExecutionStatus,
    showPermissionBanner: Boolean,
    onTabSelected: (Int) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRunShortcut: (ShortcutEntity) -> Unit,
    onEditShortcut: (ShortcutEntity) -> Unit,
    onDuplicateShortcut: (ShortcutEntity) -> Unit,
    onToggleFavorite: (ShortcutEntity) -> Unit,
    onDeleteShortcut: (Long) -> Unit,
    onCreateNewShortcut: () -> Unit,
    onToggleAutomation: (Long, Boolean) -> Unit,
    onDeleteAutomation: (Long) -> Unit,
    onTestRunAutomation: (AutomationEntity) -> Unit,
    onOpenNewAutomationDialog: () -> Unit,
    onCloseAutomationDialog: () -> Unit,
    onSaveAutomation: (AutomationEntity) -> Unit,
    onInstallGalleryTemplate: (ShortcutEntity) -> Unit,
    onClearHistory: () -> Unit,
    onRequestPermissions: () -> Unit,
    onDismissPermissionBanner: () -> Unit,
    onDismissExecutionStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            ShortcutsTopBar(
                showPermissionBanner = showPermissionBanner,
                executionStatus = executionStatus,
                onRequestPermissions = onRequestPermissions,
                onDismissPermissionBanner = onDismissPermissionBanner,
                onDismissExecutionStatus = onDismissExecutionStatus
            )
        },
        bottomBar = {
            ShortcutsBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        modifier = modifier.fillMaxSize()
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
                        onCategorySelected = onCategorySelected,
                        onSearchQueryChange = onSearchQueryChange,
                        onRunShortcut = onRunShortcut,
                        onEditShortcut = onEditShortcut,
                        onDuplicateShortcut = onDuplicateShortcut,
                        onToggleFavorite = onToggleFavorite,
                        onDeleteShortcut = onDeleteShortcut,
                        onCreateNewShortcut = onCreateNewShortcut
                    )

                    1 -> AutomationsScreen(
                        automations = automations,
                        availableShortcuts = allShortcuts,
                        showDialog = showAutomationDialog,
                        onToggleAutomation = onToggleAutomation,
                        onDeleteAutomation = onDeleteAutomation,
                        onTestRunAutomation = onTestRunAutomation,
                        onOpenNewDialog = onOpenNewAutomationDialog,
                        onCloseDialog = onCloseAutomationDialog,
                        onSaveAutomation = onSaveAutomation
                    )

                    2 -> GalleryScreen(
                        templates = galleryTemplates,
                        onInstallTemplate = onInstallGalleryTemplate,
                        onTestRunTemplate = onRunShortcut
                    )

                    3 -> HistoryScreen(
                        logs = recentLogs,
                        onClearHistory = onClearHistory
                    )
                }
            }
        }
    }
}
