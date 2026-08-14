package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DefaultShortcutsProvider
import com.example.data.model.ActionJsonHelper
import com.example.data.model.ActionType
import com.example.data.model.AutomationEntity
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutAction
import com.example.data.model.ShortcutEntity
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import com.example.engine.ExecutionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val isFavorite: Boolean = false
)

class ShortcutsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShortcutRepository
    val actionExecutor: ActionExecutor

    val allShortcuts: StateFlow<List<ShortcutEntity>>
    val allAutomations: StateFlow<List<AutomationEntity>>
    val recentLogs: StateFlow<List<ExecutionLogEntity>>
    val executionStatus: StateFlow<ExecutionStatus>

    // Navigation & UI State
    private val _selectedTab = MutableStateFlow(0) // 0: Atajos, 1: Automatizaciones, 2: Galería, 3: Historial
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private val _showAutomationDialog = MutableStateFlow(false)
    val showAutomationDialog: StateFlow<Boolean> = _showAutomationDialog.asStateFlow()

    private val _editingAutomation = MutableStateFlow<AutomationEntity?>(null)
    val editingAutomation: StateFlow<AutomationEntity?> = _editingAutomation.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = ShortcutRepository(database)
        actionExecutor = ActionExecutor(application)
        executionStatus = actionExecutor.status

        allShortcuts = repository.allShortcuts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allAutomations = repository.allAutomations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recentLogs = repository.recentLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }
    }

    val filteredShortcuts: StateFlow<List<ShortcutEntity>> = combine(
        allShortcuts,
        _selectedCategory,
        _searchQuery
    ) { shortcuts, category, query ->
        shortcuts.filter { shortcut ->
            val matchesCategory = when (category) {
                "Todos" -> true
                "Favoritos" -> shortcut.isFavorite
                else -> shortcut.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                    shortcut.title.contains(query, ignoreCase = true) ||
                    shortcut.description.contains(query, ignoreCase = true) ||
                    shortcut.category.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val galleryTemplates: List<ShortcutEntity> = DefaultShortcutsProvider.getGalleryTemplates()

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun runShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
            actionExecutor.executeShortcut(
                shortcutId = shortcut.id,
                shortcutTitle = shortcut.title,
                actions = actions
            ) { success, resultMessage, durationMs ->
                viewModelScope.launch {
                    repository.recordExecution(shortcut.id)
                    repository.logExecution(
                        ExecutionLogEntity(
                            shortcutId = shortcut.id,
                            shortcutTitle = shortcut.title,
                            iconKey = shortcut.iconKey,
                            colorHex = shortcut.colorHex,
                            timestamp = System.currentTimeMillis(),
                            status = if (success) "SUCCESS" else "FAILED",
                            actionCount = actions.size,
                            durationMs = durationMs,
                            summary = resultMessage
                        )
                    )
                }
            }
        }
    }

    fun toggleFavorite(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(shortcut.id, shortcut.isFavorite)
        }
    }

    fun deleteShortcut(id: Long) {
        viewModelScope.launch {
            repository.deleteShortcut(id)
        }
    }

    fun installGalleryTemplate(template: ShortcutEntity) {
        viewModelScope.launch {
            val newShortcut = template.copy(
                id = 0L,
                createdAt = System.currentTimeMillis(),
                runCount = 0,
                lastRunTimestamp = 0L
            )
            repository.saveShortcut(newShortcut)
            _selectedTab.value = 0 // Switch to shortcuts list
        }
    }

    fun duplicateShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            val copy = shortcut.copy(
                id = 0L,
                title = "${shortcut.title} (Copia)",
                runCount = 0,
                lastRunTimestamp = 0L,
                createdAt = System.currentTimeMillis()
            )
            repository.saveShortcut(copy)
        }
    }

    // Editor Actions
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

    fun updateEditorTitle(title: String) {
        _editorState.value = _editorState.value.copy(title = title)
    }

    fun updateEditorDescription(desc: String) {
        _editorState.value = _editorState.value.copy(description = desc)
    }

    fun updateEditorColor(colorHex: String) {
        _editorState.value = _editorState.value.copy(colorHex = colorHex)
    }

    fun updateEditorIcon(iconKey: String) {
        _editorState.value = _editorState.value.copy(iconKey = iconKey)
    }

    fun updateEditorCategory(category: String) {
        _editorState.value = _editorState.value.copy(category = category)
    }

    fun addActionToEditor(type: ActionType) {
        val defaultAction = when (type) {
            ActionType.SPEAK_TEXT -> ShortcutAction(
                type = type,
                title = "Pronunciar mensaje",
                param1 = "¡Acción ejecutada con éxito!"
            )
            ActionType.TOGGLE_FLASHLIGHT -> ShortcutAction(
                type = type,
                title = "Alternar Linterna",
                param1 = "toggle"
            )
            ActionType.VIBRATE -> ShortcutAction(
                type = type,
                title = "Vibración háptica",
                param1 = "150",
                param2 = "single"
            )
            ActionType.SHOW_NOTIFICATION -> ShortcutAction(
                type = type,
                title = "Mostrar Notificación",
                param1 = "Atajos",
                param2 = "Tarea automatizada completada"
            )
            ActionType.OPEN_URL -> ShortcutAction(
                type = type,
                title = "Abrir Enlace Web",
                param1 = "https://google.com"
            )
            ActionType.SEARCH_WEB -> ShortcutAction(
                type = type,
                title = "Buscar en la Web",
                param1 = "Noticias del día"
            )
            ActionType.COPY_CLIPBOARD -> ShortcutAction(
                type = type,
                title = "Copiar al Portapapeles",
                param1 = "Texto copiado"
            )
            ActionType.SEND_WHATSAPP -> ShortcutAction(
                type = type,
                title = "Enviar por WhatsApp",
                param1 = "",
                param2 = "¡Hola! Te escribo desde un atajo."
            )
            ActionType.SEND_SMS -> ShortcutAction(
                type = type,
                title = "Enviar SMS",
                param1 = "",
                param2 = "Mensaje automático"
            )
            ActionType.SHARE_TEXT -> ShortcutAction(
                type = type,
                title = "Compartir texto",
                param1 = "Compartido desde Atajos"
            )
            ActionType.SET_TIMER -> ShortcutAction(
                type = type,
                title = "Iniciar Temporizador",
                param1 = "300",
                param2 = "Mi Temporizador"
            )
            ActionType.WAIT_DELAY -> ShortcutAction(
                type = type,
                title = "Pausa / Esperar",
                param1 = "2"
            )
            ActionType.QUICK_CALCULATOR -> ShortcutAction(
                type = type,
                title = "Calcular Propina",
                param1 = "50",
                param2 = "15"
            )
        }
        val current = _editorState.value.actions.toMutableList()
        current.add(defaultAction)
        _editorState.value = _editorState.value.copy(actions = current)
    }

    fun removeActionFromEditor(index: Int) {
        val current = _editorState.value.actions.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _editorState.value = _editorState.value.copy(actions = current)
        }
    }

    fun updateActionInEditor(index: Int, updatedAction: ShortcutAction) {
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

    fun saveEditorShortcut() {
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
        viewModelScope.launch {
            repository.saveShortcut(shortcut)
            closeEditor()
        }
    }

    fun testRunEditorShortcut() {
        val state = _editorState.value
        val title = state.title.ifBlank { "Prueba de Atajo" }
        viewModelScope.launch {
            actionExecutor.executeShortcut(
                shortcutId = state.id,
                shortcutTitle = title,
                actions = state.actions
            ) { _, _, _ -> }
        }
    }

    // Automations
    fun openNewAutomationDialog() {
        _editingAutomation.value = null
        _showAutomationDialog.value = true
    }

    fun openEditAutomationDialog(automation: AutomationEntity) {
        _editingAutomation.value = automation
        _showAutomationDialog.value = true
    }

    fun closeAutomationDialog() {
        _showAutomationDialog.value = false
        _editingAutomation.value = null
    }

    fun saveAutomation(automation: AutomationEntity) {
        viewModelScope.launch {
            repository.saveAutomation(automation)
            closeAutomationDialog()
        }
    }

    fun toggleAutomation(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAutomation(id, enabled)
        }
    }

    fun deleteAutomation(id: Long) {
        viewModelScope.launch {
            repository.deleteAutomation(id)
        }
    }

    fun testRunAutomation(automation: AutomationEntity) {
        viewModelScope.launch {
            val shortcut = repository.getShortcutById(automation.shortcutId)
            if (shortcut != null) {
                runShortcut(shortcut)
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun dismissExecutionStatus() {
        actionExecutor.dismissStatus()
    }

    override fun onCleared() {
        super.onCleared()
        actionExecutor.release()
    }
}
