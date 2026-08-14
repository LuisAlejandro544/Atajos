package com.example.engine.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.ActionJsonHelper
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutTrigger
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver nativo del sistema para capturar la conexión o desconexión del cargador
 * y ejecutar automáticamente los atajos que tengan configurado este disparador.
 */
class PowerTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val triggerType = when (action) {
            Intent.ACTION_POWER_CONNECTED -> ShortcutTrigger.POWER_CONNECTED.key
            Intent.ACTION_POWER_DISCONNECTED -> ShortcutTrigger.POWER_DISCONNECTED.key
            else -> return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val database = AppDatabase.getInstance(context.applicationContext)
                val repository = ShortcutRepository(database)
                val matchingShortcuts = repository.getShortcutsForPowerTrigger(triggerType)

                if (matchingShortcuts.isEmpty()) {
                    Log.d(TAG, "No hay atajos configurados para el evento: $triggerType")
                    return@launch
                }

                val actionExecutor = ActionExecutor(context.applicationContext)

                for (shortcut in matchingShortcuts) {
                    val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
                    if (actions.isEmpty()) continue

                    val startTime = System.currentTimeMillis()
                    actionExecutor.executeShortcut(
                        shortcutId = shortcut.id,
                        shortcutTitle = shortcut.title,
                        actions = actions
                    ) { success, resultMessage, durationMs ->
                        scope.launch {
                            if (success) {
                                repository.recordExecution(shortcut.id)
                            }
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
                                    summary = "[Disparador: ${if (triggerType == ShortcutTrigger.POWER_CONNECTED.key) "Cargador Conectado" else "Cargador Desconectado"}] $resultMessage"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ejecutando atajo disparado por alimentación", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "PowerTriggerReceiver"
    }
}
