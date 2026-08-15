package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import com.example.engine.AppScannerHelper
import com.example.ui.components.apppicker.AppPickerFilterRow
import com.example.ui.components.apppicker.AppPickerHeader
import com.example.ui.components.apppicker.AppPickerListItem
import com.example.ui.components.apppicker.AppPickerLoadingState
import com.example.ui.components.apppicker.AppPickerSearchBar

/**
 * Selector modal modularizado de juegos y aplicaciones con filtros, buscador y escaneo asíncrono.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(
    selectedPackageName: String,
    onSelectApp: (AppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var appsList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, GAMES, APPS

    LaunchedEffect(Unit) {
        isLoading = true
        appsList = AppScannerHelper.getInstalledApps(context)
        isLoading = false
    }

    val gamesCount = remember(appsList) { appsList.count { it.isGame } }
    val normalAppsCount = remember(appsList) { appsList.count { !it.isGame } }

    val filteredApps = remember(appsList, searchQuery, filterType) {
        appsList.filter { app ->
            val matchesFilter = when (filterType) {
                "GAMES" -> app.isGame
                "APPS" -> !app.isGame
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    app.name.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("app_picker_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            AppPickerHeader(onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                AppPickerLoadingState()
            } else {
                AppPickerSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                AppPickerFilterRow(
                    totalAppsCount = appsList.size,
                    gamesCount = gamesCount,
                    normalAppsCount = normalAppsCount,
                    selectedFilter = filterType,
                    onFilterSelected = { filterType = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron aplicaciones con ese filtro",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(340.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            AppPickerListItem(
                                app = app,
                                isSelected = app.packageName == selectedPackageName,
                                onSelectApp = onSelectApp,
                                onDismiss = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}
