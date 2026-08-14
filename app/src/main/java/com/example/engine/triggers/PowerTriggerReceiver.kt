package com.example.engine.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
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
 * BroadcastReceiver nativo del sistema para capturar eventos de alimentación y estado de batería
 * (conexión/desconexión de corriente, batería baja, batería restablecida o carga completa al 100%)
 * y ejecutar automáticamente los atajos configurados en segundo plano.
 */
class PowerTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val triggerType = when (action) {
            Intent.ACTION_POWER_CONNECTED -> ShortcutTrigger.POWER_CONNECTED.key
            Intent.ACTION_POWER_DISCONNECTED -> ShortcutTrigger.POWER_DISCONNECTED.key
            Intent.ACTION_BATTERY_LOW -> ShortcutTrigger.BATTERY_LOW.key
            Intent.ACTION_BATTERY_OKAY -> ShortcutTrigger.BATTERY_OK.key
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val batteryPct = if (scale > 0) (level * 100) / scale else -1
                if (batteryPct == 100 || status == BatteryManager.BATTERY_STATUS_FULL) {
                    ShortcutTrigger.BATTERY_FULL.key
                } else {
                    return
                }
            }
            else -> return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val database = AppDatabase.getInstance(context.applicationContext)
                val repository = ShortcutRepository(database)
                val matchingShortcuts = repository.getShortcutsForTrigger(triggerType)

                if (matchingShortcuts.isEmpty()) {
                    Log.d(TAG, "No hay atajos configurados para el evento: $triggerType")
                    return@launch
                }

                val actionExecutor = ActionExecutor(context.applicationContext)
                val triggerLabel = when (triggerType) {
                    ShortcutTrigger.POWER_CONNECTED.key -> "Cargador Conectado"
                    ShortcutTrigger.POWER_DISCONNECTED.key -> "Cargador Desconectado"
                    ShortcutTrigger.BATTERY_LOW.key -> "Batería Baja"
                    ShortcutTrigger.BATTERY_OK.key -> "Batería Restablecida"
                    ShortcutTrigger.BATTERY_FULL.key -> "Batería 100%"
                    else -> triggerType
                }

                for (shortcut in matchingShortcuts) {
                    val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
                    if (actions.isEmpty()) continue

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
                                    summary = "[Disparador: $triggerLabel] $resultMessage"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ejecutando atajo disparado por evento de energía/batería", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "PowerTriggerReceiver"
    }
}
