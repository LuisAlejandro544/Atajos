package com.example.ui.viewmodel

import com.example.data.model.ActionJsonHelper
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutEntity
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Gestor modular de ejecución de atajos, control de ciclo de vida de corrutinas (Job)
 * y persistencia de logs de auditoría en Room.
 */
class ShortcutExecutionManager(
    private val repository: ShortcutRepository,
    private val actionExecutor: ActionExecutor,
    private val scope: CoroutineScope
) {
    private var activeExecutionJob: Job? = null

    fun runShortcut(shortcut: ShortcutEntity) {
        activeExecutionJob?.cancel()
        activeExecutionJob = scope.launch {
            val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
            actionExecutor.executeShortcut(
                shortcutId = shortcut.id,
                shortcutTitle = shortcut.title,
                actions = actions
            ) { success, resultMessage, durationMs ->
                scope.launch {
                    val isCancelled = resultMessage.contains("Cancelado", ignoreCase = true)
                    if (success && !isCancelled) {
                        repository.recordExecution(shortcut.id)
                    }
                    val statusStr = when {
                        isCancelled -> "CANCELLED"
                        success -> "SUCCESS"
                        else -> "FAILED"
                    }
                    repository.logExecution(
                        ExecutionLogEntity(
                            shortcutId = shortcut.id,
                            shortcutTitle = shortcut.title,
                            iconKey = shortcut.iconKey,
                            colorHex = shortcut.colorHex,
                            timestamp = System.currentTimeMillis(),
                            status = statusStr,
                            actionCount = actions.size,
                            durationMs = durationMs,
                            summary = resultMessage
                        )
                    )
                }
            }
        }
    }

    fun runShortcutById(shortcutId: Long) {
        scope.launch {
            val shortcut = repository.getShortcutById(shortcutId)
            if (shortcut != null) {
                runShortcut(shortcut)
            }
        }
    }

    fun cancelExecution() {
        actionExecutor.cancelExecution()
        activeExecutionJob?.cancel()
        activeExecutionJob = null
    }
}
