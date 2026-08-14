package com.example.ui.viewmodel

import com.example.data.model.ActionJsonHelper
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.data.model.ShortcutEntity
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gestor modular que encapsula el estado y las operaciones del editor de atajos.
 */
class ShortcutEditorManager(
    private val repository: ShortcutRepository,
    private val actionExecutor: ActionExecutor,
    private val coroutineScope: CoroutineScope
) {
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    fun openNewShortcutEditor() {
        _editorState.value = EditorState(
            isEditing = true,
            isNew = true,
            title = "",
            description = "",
            colorHex = "#4F46E5",
            iconKey = "flash_on",
            category = "Utilidades",
            actions = listOf(
                ShortcutAction(
                    type = ActionType.VIBRATE,
                    title = "Vibración de aviso",
                    param1 = "150",
                    param2 = "single"
                )
            ),
            isFavorite = false
        )
    }

    fun openEditShortcut(shortcut: ShortcutEntity) {
        _editorState.value = EditorState(
            isEditing = true,
            isNew = false,
            id = shortcut.id,
            title = shortcut.title,
            description = shortcut.description,
            colorHex = shortcut.colorHex,
            iconKey = shortcut.iconKey,
            category = shortcut.category,
            actions = ActionJsonHelper.fromJson(shortcut.actionsJson),
            isFavorite = shortcut.isFavorite
        )
    }

    fun closeEditor() {
        _editorState.value = EditorState(isEditing = false)
    }

    fun updateTitle(title: String) {
        _editorState.value = _editorState.value.copy(title = title)
    }

    fun updateDescription(desc: String) {
        _editorState.value = _editorState.value.copy(description = desc)
    }

    fun updateColor(colorHex: String) {
        _editorState.value = _editorState.value.copy(colorHex = colorHex)
    }

    fun updateIcon(iconKey: String) {
        _editorState.value = _editorState.value.copy(iconKey = iconKey)
    }

    fun updateCategory(category: String) {
        _editorState.value = _editorState.value.copy(category = category)
    }

    fun addAction(type: ActionType) {
        val defaultAction = DefaultActionFactory.createDefaultAction(type)
        val current = _editorState.value.actions.toMutableList()
        current.add(defaultAction)
        _editorState.value = _editorState.value.copy(actions = current)
    }

    fun removeAction(index: Int) {
        val current = _editorState.value.actions.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _editorState.value = _editorState.value.copy(actions = current)
        }
    }

    fun updateAction(index: Int, updatedAction: ShortcutAction) {
        val current = _editorState.value.actions.toMutableList()
        if (index in current.indices) {
            current[index] = updatedAction
            _editorState.value = _editorState.value.copy(actions = current)
        }
    }

    fun moveActionUp(index: Int) {
        if (index > 0) {
            val current = _editorState.value.actions.toMutableList()
            val item = current.removeAt(index)
            current.add(index - 1, item)
            _editorState.value = _editorState.value.copy(actions = current)
        }
    }

    fun moveActionDown(index: Int) {
        val current = _editorState.value.actions.toMutableList()
        if (index < current.size - 1) {
            val item = current.removeAt(index)
            current.add(index + 1, item)
            _editorState.value = _editorState.value.copy(actions = current)
        }
    }

    fun saveShortcut(onSaved: () -> Unit = {}) {
        val state = _editorState.value
        val title = state.title.ifBlank { "Mi Atajo Personal" }
        val shortcut = ShortcutEntity(
            id = state.id,
            title = title,
            description = state.description,
            colorHex = state.colorHex,
            iconKey = state.iconKey,
            category = state.category.ifBlank { "General" },
            actionsJson = ActionJsonHelper.toJson(state.actions),
            isFavorite = state.isFavorite,
            createdAt = System.currentTimeMillis()
        )
        coroutineScope.launch {
            repository.saveShortcut(shortcut)
            closeEditor()
            onSaved()
        }
    }

    private var testRunJob: kotlinx.coroutines.Job? = null

    fun testRunShortcut() {
        val state = _editorState.value
        val title = state.title.ifBlank { "Prueba de Atajo" }
        testRunJob?.cancel()
        testRunJob = coroutineScope.launch {
            actionExecutor.executeShortcut(
                shortcutId = state.id,
                shortcutTitle = title,
                actions = state.actions
            ) { _, _, _ -> }
        }
    }
}
