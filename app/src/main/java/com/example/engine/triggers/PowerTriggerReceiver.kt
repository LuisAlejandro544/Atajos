package com.example.engine.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.ActionJsonHelper
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutEntity
import com.example.data.model.ShortcutTrigger
import com.example.data.model.TriggerType
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver nativo del sistema para capturar eventos de alimentación y estado de batería
 * (conexión/desconexión de corriente, batería baja, batería restablecida o carga completa al 100%)
 * y ejecutar automáticamente tanto los atajos con disparador directo como las automatizaciones activas en segundo plano.
 */
class PowerTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val (triggerTypeKey, autoTriggerType) = when (action) {
            Intent.ACTION_POWER_CONNECTED -> Pair(ShortcutTrigger.POWER_CONNECTED.key, TriggerType.CHARGER_CONNECTED)
            Intent.ACTION_POWER_DISCONNECTED -> Pair(ShortcutTrigger.POWER_DISCONNECTED.key, TriggerType.CHARGER_DISCONNECTED)
            Intent.ACTION_BATTERY_LOW -> Pair(ShortcutTrigger.BATTERY_LOW.key, TriggerType.BATTERY_LOW)
            Intent.ACTION_BATTERY_OKAY -> Pair(ShortcutTrigger.BATTERY_OK.key, TriggerType.BATTERY_OK)
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val batteryPct = if (scale > 0) (level * 100) / scale else -1
                if (batteryPct == 100 || status == BatteryManager.BATTERY_STATUS_FULL) {
                    Pair(ShortcutTrigger.BATTERY_FULL.key, TriggerType.BATTERY_FULL)
                } else {
                    return
                }
            }
            else -> return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            val actionExecutor = ActionExecutor(appContext)
            try {
                val database = AppDatabase.getInstance(appContext)
                val repository = ShortcutRepository(database)

                // 1. Obtener atajos configurados directamente con este disparador
                val directShortcuts = repository.getShortcutsForTrigger(triggerTypeKey)

                // 2. Obtener automatizaciones activas configuradas para este evento
                val activeAutomations = repository.getActiveAutomationsByTriggerType(autoTriggerType)

                // 3. Unificar la lista para evitar ejecuciones duplicadas del mismo atajo
                val shortcutsToRun = mutableListOf<Pair<ShortcutEntity, String>>()
                val seenShortcutIds = mutableSetOf<Long>()

                for (shortcut in directShortcuts) {
                    if (seenShortcutIds.add(shortcut.id)) {
                        shortcutsToRun.add(Pair(shortcut, "Atajo Directo"))
                    }
                }

                for (automation in activeAutomations) {
                    val shortcut = repository.getShortcutById(automation.shortcutId)
                    if (shortcut != null && seenShortcutIds.add(shortcut.id)) {
                        shortcutsToRun.add(Pair(shortcut, automation.title))
                    }
                }

                if (shortcutsToRun.isEmpty()) {
                    Log.d(TAG, "No hay atajos ni automatizaciones activas para el evento: $triggerTypeKey")
                    return@launch
                }

                val triggerLabel = when (triggerTypeKey) {
                    ShortcutTrigger.POWER_CONNECTED.key -> "Cargador Conectado"
                    ShortcutTrigger.POWER_DISCONNECTED.key -> "Cargador Desconectado"
                    ShortcutTrigger.BATTERY_LOW.key -> "Batería Baja"
                    ShortcutTrigger.BATTERY_OK.key -> "Batería Restablecida"
                    ShortcutTrigger.BATTERY_FULL.key -> "Batería 100%"
                    else -> triggerTypeKey
                }

                for ((shortcut, sourceName) in shortcutsToRun) {
                    val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
                    if (actions.isEmpty()) continue

                    var execSuccess = false
                    var execMessage = ""
                    var execDuration = 0L

                    actionExecutor.executeShortcut(
                        shortcutId = shortcut.id,
                        shortcutTitle = shortcut.title,
                        actions = actions
                    ) { success, resultMessage, durationMs ->
                        execSuccess = success
                        execMessage = resultMessage
                        execDuration = durationMs
                    }

                    // Registrar de forma síncrona en la base de datos antes de finalizar el receiver
                    try {
                        if (execSuccess) {
                            repository.recordExecution(shortcut.id)
                        }
                        repository.logExecution(
                            ExecutionLogEntity(
                                shortcutId = shortcut.id,
                                shortcutTitle = shortcut.title,
                                iconKey = shortcut.iconKey,
                                colorHex = shortcut.colorHex,
                                timestamp = System.currentTimeMillis(),
                                status = if (execSuccess) "SUCCESS" else "FAILED",
                                actionCount = actions.size,
                                durationMs = execDuration,
                                summary = "[Disparador: $triggerLabel | $sourceName] $execMessage"
                            )
                        )
                    } catch (dbErr: Exception) {
                        Log.e(TAG, "Error guardando log de ejecución en base de datos", dbErr)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ejecutando atajo disparado por evento de energía/batería", e)
            } finally {
                try {
                    actionExecutor.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error liberando ActionExecutor", e)
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "PowerTriggerReceiver"
    }
}
