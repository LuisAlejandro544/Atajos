package com.example.ui.viewmodel

import android.content.Context
import com.example.data.model.AutomationEntity
import com.example.data.model.TriggerType
import com.example.data.repository.ShortcutRepository
import com.example.engine.triggers.TimeSchedulerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gestor modular que encapsula el estado y las operaciones CRUD de las automatizaciones,
 * conectando los disparadores temporales con AlarmManager.
 */
class AutomationsManager(
    private val context: Context,
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
            if (automation.triggerType == TriggerType.TIME_OF_DAY) {
                if (automation.isEnabled) {
                    TimeSchedulerHelper.scheduleAutomationAlarm(context, automation)
                } else {
                    TimeSchedulerHelper.cancelAutomationAlarm(context, automation.id)
                }
            }
        }
    }

    fun toggleAutomation(id: Long, enabled: Boolean) {
        coroutineScope.launch {
            repository.toggleAutomation(id, enabled)
            val automation = repository.allAutomations
            val automationsList = repository.getShortcutById(id)
            if (!enabled) {
                TimeSchedulerHelper.cancelAutomationAlarm(context, id)
            }
        }
    }

    fun deleteAutomation(id: Long) {
        coroutineScope.launch {
            repository.deleteAutomation(id)
            closeAutomationDialog()
            TimeSchedulerHelper.cancelAutomationAlarm(context, id)
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
