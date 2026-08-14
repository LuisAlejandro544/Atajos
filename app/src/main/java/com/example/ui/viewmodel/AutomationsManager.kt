package com.example.ui.viewmodel

import com.example.data.model.AutomationEntity
import com.example.data.repository.ShortcutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gestor modular que encapsula el estado y las operaciones CRUD de las automatizaciones.
 */
class AutomationsManager(
    private val repository: ShortcutRepository,
    private val coroutineScope: CoroutineScope,
    private val onRunShortcutById: (Long) -> Unit
) {
    private val _showAutomationDialog = MutableStateFlow(false)
    val showAutomationDialog: StateFlow<Boolean> = _showAutomationDialog.asStateFlow()

    private val _editingAutomation = MutableStateFlow<AutomationEntity?>(null)
    val editingAutomation: StateFlow<AutomationEntity?> = _editingAutomation.asStateFlow()

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
        coroutineScope.launch {
            repository.saveAutomation(automation)
            closeAutomationDialog()
        }
    }

    fun toggleAutomation(id: Long, enabled: Boolean) {
        coroutineScope.launch {
            repository.toggleAutomation(id, enabled)
        }
    }

    fun deleteAutomation(id: Long) {
        coroutineScope.launch {
            repository.deleteAutomation(id)
        }
    }

    fun testRunAutomation(automation: AutomationEntity) {
        coroutineScope.launch {
            val shortcut = repository.getShortcutById(automation.shortcutId)
            if (shortcut != null) {
                onRunShortcutById(shortcut.id)
            }
        }
    }
}
