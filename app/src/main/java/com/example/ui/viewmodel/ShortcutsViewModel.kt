package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DefaultShortcutsProvider
import com.example.data.model.ActionType
import com.example.data.model.AutomationEntity
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutAction
import com.example.data.model.ShortcutEntity
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import com.example.engine.AppShortcutsHelper
import com.example.engine.ExecutionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal que centraliza los flujos de datos reactivos (Room + StateFlow)
 * y delega la lógica en gestores especializados (ShortcutEditorManager, AutomationsManager, ShortcutExecutionManager).
 */
class ShortcutsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShortcutRepository
    val actionExecutor: ActionExecutor

    // Gestores modulares
    private val editorManager: ShortcutEditorManager
    private val automationsManager: AutomationsManager
    private val executionManager: ShortcutExecutionManager

    // Flujos de datos reactivos principales
    val allShortcuts: StateFlow<List<ShortcutEntity>>
    val allAutomations: StateFlow<List<AutomationEntity>>
    val recentLogs: StateFlow<List<ExecutionLogEntity>>
    val executionStatus: StateFlow<ExecutionStatus>

    // Control de pestañas y filtros
    private val _selectedTab = MutableStateFlow(0) // 0: Atajos, 1: Automatizaciones, 2: Galería, 3: Historial
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Delegación de estados de gestores
    val editorState: StateFlow<EditorState>
        get() = editorManager.editorState

    val showAutomationDialog: StateFlow<Boolean>
        get() = automationsManager.showAutomationDialog

    val editingAutomation: StateFlow<AutomationEntity?>
        get() = automationsManager.editingAutomation

    init {
        val database = AppDatabase.getInstance(application)
        repository = ShortcutRepository(database)
        actionExecutor = ActionExecutor(application)
        executionStatus = actionExecutor.status

        executionManager = ShortcutExecutionManager(repository, actionExecutor, viewModelScope)
        editorManager = ShortcutEditorManager(application, repository, actionExecutor, viewModelScope)
        automationsManager = AutomationsManager(application, repository, viewModelScope) { shortcutId ->
            executionManager.runShortcutById(shortcutId)
        }

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

        // Sincronizar App Shortcuts en el launcher del dispositivo cuando cambien los atajos
        viewModelScope.launch {
            allShortcuts.collect { shortcuts ->
                if (shortcuts.isNotEmpty()) {
                    AppShortcutsHelper.updateDynamicShortcuts(getApplication(), shortcuts)
                }
            }
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

    // ── Navegación & Filtros ──────────────────────────────────────────────────
    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ── Operaciones y Ejecución de Atajos ─────────────────────────────────────
    fun runShortcut(shortcut: ShortcutEntity) = executionManager.runShortcut(shortcut)

    fun runShortcutById(shortcutId: Long) = executionManager.runShortcutById(shortcutId)

    fun cancelExecution() = executionManager.cancelExecution()

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
            _selectedTab.value = 0
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

    // ── Delegaciones al Editor de Atajos ─────────────────────────────────────
    fun openNewShortcutEditor() = editorManager.openNewShortcutEditor()
    fun openEditShortcut(shortcut: ShortcutEntity) = editorManager.openEditShortcut(shortcut)
    fun closeEditor() = editorManager.closeEditor()
    fun updateEditorTitle(title: String) = editorManager.updateTitle(title)
    fun updateEditorDescription(desc: String) = editorManager.updateDescription(desc)
    fun updateEditorColor(colorHex: String) = editorManager.updateColor(colorHex)
    fun updateEditorIcon(iconKey: String) = editorManager.updateIcon(iconKey)
    fun updateEditorCategory(category: String) = editorManager.updateCategory(category)
    fun updateEditorTrigger(trigger: String) = editorManager.updateTrigger(trigger)
    fun addActionToEditor(type: ActionType) = editorManager.addAction(type)
    fun removeActionFromEditor(index: Int) = editorManager.removeAction(index)
    fun updateActionInEditor(index: Int, updatedAction: ShortcutAction) = editorManager.updateAction(index, updatedAction)
    fun moveActionUp(index: Int) = editorManager.moveActionUp(index)
    fun moveActionDown(index: Int) = editorManager.moveActionDown(index)
    fun saveEditorShortcut() = editorManager.saveShortcut()
    fun testRunEditorShortcut() = editorManager.testRunShortcut()

    // ── Delegaciones a Automatizaciones ──────────────────────────────────────
    fun openNewAutomationDialog() = automationsManager.openNewAutomationDialog()
    fun openEditAutomationDialog(automation: AutomationEntity) = automationsManager.openEditAutomationDialog(automation)
    fun closeAutomationDialog() = automationsManager.closeAutomationDialog()
    fun saveAutomation(automation: AutomationEntity) = automationsManager.saveAutomation(automation)
    fun toggleAutomation(id: Long, enabled: Boolean) = automationsManager.toggleAutomation(id, enabled)
    fun deleteAutomation(id: Long) = automationsManager.deleteAutomation(id)
    fun testRunAutomation(automation: AutomationEntity) = automationsManager.testRunAutomation(automation)

    // ── Historial & Estado ───────────────────────────────────────────────────
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
