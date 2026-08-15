package com.example.ui.components.apppicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fila horizontal modular con FilterChips para filtrar apps por "Todas", "Juegos" y "Apps normales".
 */
@Composable
fun AppPickerFilterRow(
    totalAppsCount: Int,
    gamesCount: Int,
    normalAppsCount: Int,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { onFilterSelected("ALL") },
                label = { Text("Todas ($totalAppsCount)") }
            )
        }
        if (gamesCount > 0) {
            item {
                FilterChip(
                    selected = selectedFilter == "GAMES",
                    onClick = { onFilterSelected("GAMES") },
                    label = { Text("🎮 Juegos ($gamesCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        item {
            FilterChip(
                selected = selectedFilter == "APPS",
                onClick = { onFilterSelected("APPS") },
                label = { Text("📱 Apps ($normalAppsCount)") }
            )
        }
    }
}
