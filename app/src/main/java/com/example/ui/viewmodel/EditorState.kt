package com.example.ui.viewmodel

import com.example.data.model.ShortcutAction

/**
 * Estado inmutable de la pantalla de creación / edición de un atajo.
 */
data class EditorState(
    val isEditing: Boolean = false,
    val isNew: Boolean = true,
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val colorHex: String = "#4F46E5",
    val iconKey: String = "flash_on",
    val category: String = "General",
    val actions: List<ShortcutAction> = emptyList(),
    val isFavorite: Boolean = false,
    val trigger: String = "NONE"
)
