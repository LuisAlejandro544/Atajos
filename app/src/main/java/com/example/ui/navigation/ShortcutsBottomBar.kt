package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary

/**
 * Barra de navegación inferior modular con soporte para testTag y diseño Material Design 3.
 */
@Composable
fun ShortcutsBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = modifier.testTag("bottom_navigation_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
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
            onClick = { onTabSelected(1) },
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
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
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
}
