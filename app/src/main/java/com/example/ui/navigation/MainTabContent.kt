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
 * Contenedor principal de pestañas modularizado con Scaffold, TopBar, BottomBar y transiciones animadas.
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
    actions: MainTabActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            ShortcutsTopBar(
                showPermissionBanner = showPermissionBanner,
                executionStatus = executionStatus,
                onRequestPermissions = actions.topBar.onRequestPermissions,
                onDismissPermissionBanner = actions.topBar.onDismissPermissionBanner,
                onDismissExecutionStatus = actions.topBar.onDismissExecutionStatus
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
                        onCategorySelected = actions.home.onCategorySelected,
                        onSearchQueryChange = actions.home.onSearchQueryChange,
                        onRunShortcut = actions.home.onRunShortcut,
                        onEditShortcut = actions.home.onEditShortcut,
                        onDuplicateShortcut = actions.home.onDuplicateShortcut,
                        onToggleFavorite = actions.home.onToggleFavorite,
                        onDeleteShortcut = actions.home.onDeleteShortcut,
                        onCreateNewShortcut = actions.home.onCreateNewShortcut
                    )

                    1 -> AutomationsScreen(
                        automations = automations,
                        availableShortcuts = allShortcuts,
                        showDialog = showAutomationDialog,
                        onToggleAutomation = actions.automations.onToggleAutomation,
                        onDeleteAutomation = actions.automations.onDeleteAutomation,
                        onTestRunAutomation = actions.automations.onTestRunAutomation,
                        onOpenNewDialog = actions.automations.onOpenNewDialog,
                        onCloseDialog = actions.automations.onCloseDialog,
                        onSaveAutomation = actions.automations.onSaveAutomation
                    )

                    2 -> GalleryScreen(
                        templates = galleryTemplates,
                        onInstallTemplate = actions.gallery.onInstallTemplate,
                        onTestRunTemplate = actions.gallery.onTestRunTemplate
                    )

                    3 -> HistoryScreen(
                        logs = recentLogs,
                        onClearHistory = actions.history.onClearHistory
                    )
                }
            }
        }
    }
}
