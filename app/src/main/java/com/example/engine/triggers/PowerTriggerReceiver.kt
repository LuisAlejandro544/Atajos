package com.example.engine.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.ActionJsonHelper
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutEntity
import com.example.data.model.ShortcutTrigger
import com.example.data.model.TriggerType
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import com.example.engine.service.AutomationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver nativo del sistema para capturar eventos de alimentación y estado de batería
 * (conexión/desconexión de corriente, porcentaje exacto de batería, batería baja, batería restablecida o carga completa al 100%)
 * y ejecutar automáticamente tanto los atajos con disparador directo como las automatizaciones activas en segundo plano.
 *
 * Utiliza WakeLock temporal para asegurar que la CPU permanezca activa con la pantalla bloqueada o apagada.
 */
class PowerTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        var exactBatteryPct: Int? = null
        var triggerTypeKey: String? = null
        var autoTriggerType: TriggerType? = null

        when (action) {
            Intent.ACTION_POWER_CONNECTED, "android.intent.action.POWER_CONNECTED" -> {
                triggerTypeKey = ShortcutTrigger.POWER_CONNECTED.key
                autoTriggerType = TriggerType.CHARGER_CONNECTED
            }
            Intent.ACTION_POWER_DISCONNECTED, "android.intent.action.POWER_DISCONNECTED" -> {
                triggerTypeKey = ShortcutTrigger.POWER_DISCONNECTED.key
                autoTriggerType = TriggerType.CHARGER_DISCONNECTED
            }
            Intent.ACTION_BATTERY_LOW -> {
                triggerTypeKey = ShortcutTrigger.BATTERY_LOW.key
                autoTriggerType = TriggerType.BATTERY_LOW
            }
            Intent.ACTION_BATTERY_OKAY -> {
                triggerTypeKey = ShortcutTrigger.BATTERY_OK.key
                autoTriggerType = TriggerType.BATTERY_OK
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val batteryPct = if (scale > 0) (level * 100) / scale else -1

                if (batteryPct >= 0) {
                    exactBatteryPct = batteryPct
                    if (batteryPct == 100 || status == BatteryManager.BATTERY_STATUS_FULL) {
                        triggerTypeKey = ShortcutTrigger.BATTERY_FULL.key
                        autoTriggerType = TriggerType.BATTERY_FULL
                    }
                } else {
                    return
                }
            }
            else -> return
        }

        // Debounce para porcentaje de batería idéntico
        val now = System.currentTimeMillis()
        if (exactBatteryPct != null) {
            val lastPct = lastProcessedBatteryPct
            if (lastPct == exactBatteryPct && (now - lastProcessedTimestamp) < 30000L) {
                // Mismo porcentaje recibido en menos de 30s, evitar duplicados innecesarios
                return
            }
            lastProcessedBatteryPct = exactBatteryPct
            lastProcessedTimestamp = now
        } else if (action == Intent.ACTION_POWER_CONNECTED || action == Intent.ACTION_POWER_DISCONNECTED ||
            action == "android.intent.action.POWER_CONNECTED" || action == "android.intent.action.POWER_DISCONNECTED"
        ) {
            val normalizedAction = if (action.contains("DISCONNECTED", ignoreCase = true)) "DISCONNECTED" else "CONNECTED"
            if (lastProcessedPowerAction == normalizedAction && (now - lastProcessedPowerTimestamp) < 1500L) {
                Log.d(TAG, "Debounce: Ignorando evento duplicado de energía recibido en <1.5s")
                return
            }
            lastProcessedPowerAction = normalizedAction
            lastProcessedPowerTimestamp = now
        }

        // Adquirir WakeLock temporal de 15s para garantizar que la CPU no se duerma con la pantalla bloqueada
        val appContext = context.applicationContext
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Atajos:PowerTriggerWakeLock"
        )?.apply {
            setReferenceCounted(false)
            try {
                acquire(15000L) // 15 segundos de timeout máximo de seguridad
            } catch (e: Exception) {
                Log.e(TAG, "Error adquiriendo WakeLock", e)
            }
        }

        // Asegurar que el servicio en segundo plano esté activo
        try {
            AutomationService.start(appContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error asegurando AutomationService", e)
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            val actionExecutor = ActionExecutor.getInstance(appContext)
            try {
                val database = AppDatabase.getInstance(appContext)
                val repository = ShortcutRepository(database)

                val shortcutsToRun = mutableListOf<Pair<ShortcutEntity, String>>()
                val seenShortcutIds = mutableSetOf<Long>()

                // 1. Atajos por evento estándar (Cargador conectado/desconectado incluye POWER_BOTH, Batería baja, etc.)
                if (triggerTypeKey != null) {
                    val directShortcuts = if (triggerTypeKey == ShortcutTrigger.POWER_CONNECTED.key || triggerTypeKey == ShortcutTrigger.POWER_DISCONNECTED.key) {
                        repository.getShortcutsForPowerTrigger(triggerTypeKey)
                    } else {
                        repository.getShortcutsForTrigger(triggerTypeKey)
                    }
                    for (s in directShortcuts) {
                        if (seenShortcutIds.add(s.id)) {
                            shortcutsToRun.add(Pair(s, "Atajo Directo"))
                        }
                    }
                }

                // 2. Atajos con disparador de porcentaje exacto (BATTERY_LEVEL:XX)
                if (exactBatteryPct != null) {
                    val targetKey = "BATTERY_LEVEL:$exactBatteryPct"
                    val batteryLevelShortcuts = repository.getBatteryLevelShortcuts().filter {
                        it.trigger.equals(targetKey, ignoreCase = true)
                    }
                    for (s in batteryLevelShortcuts) {
                        if (seenShortcutIds.add(s.id)) {
                            shortcutsToRun.add(Pair(s, "Batería al $exactBatteryPct%"))
                        }
                    }
                }

                // 3. Automatizaciones activas configuradas para este evento
                if (autoTriggerType != null) {
                    val activeAutomations = if (autoTriggerType == TriggerType.CHARGER_CONNECTED || autoTriggerType == TriggerType.CHARGER_DISCONNECTED) {
                        repository.getActiveAutomationsForPowerTrigger(autoTriggerType)
                    } else {
                        repository.getActiveAutomationsByTriggerType(autoTriggerType)
                    }
                    for (automation in activeAutomations) {
                        val shortcut = repository.getShortcutById(automation.shortcutId)
                        if (shortcut != null && seenShortcutIds.add(shortcut.id)) {
                            shortcutsToRun.add(Pair(shortcut, automation.title))
                        }
                    }
                }

                if (shortcutsToRun.isEmpty()) {
                    Log.d(TAG, "No hay atajos ni automatizaciones configuradas para el evento $action")
                    return@launch
                }

                val triggerLabel = when {
                    exactBatteryPct != null && triggerTypeKey == null -> "Batería al $exactBatteryPct%"
                    triggerTypeKey == ShortcutTrigger.POWER_CONNECTED.key -> "Cargador Conectado"
                    triggerTypeKey == ShortcutTrigger.POWER_DISCONNECTED.key -> "Cargador Desconectado"
                    triggerTypeKey == ShortcutTrigger.BATTERY_LOW.key -> "Batería Baja"
                    triggerTypeKey == ShortcutTrigger.BATTERY_OK.key -> "Batería Restablecida"
                    triggerTypeKey == ShortcutTrigger.BATTERY_FULL.key -> "Batería 100%"
                    else -> triggerTypeKey ?: "Evento de Batería"
                }

                for ((shortcut, sourceName) in shortcutsToRun) {
                    val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
                    if (actions.isEmpty()) continue

                    var execSuccess = false
                    var execMessage = ""
                    var execDuration = 0L

                    actionExecutor.executeShortcut(
                        shortcutId = shortcut.id,
                        shortcutTitle = "${shortcut.title} (⚡ $triggerLabel)",
                        actions = actions
                    ) { success, resultMessage, durationMs ->
                        execSuccess = success
                        execMessage = resultMessage
                        execDuration = durationMs
                    }

                    // Registrar en base de datos
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
                    if (wakeLock != null && wakeLock.isHeld) {
                        wakeLock.release()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error liberando WakeLock", e)
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "PowerTriggerReceiver"
        private var lastProcessedBatteryPct: Int? = null
        private var lastProcessedTimestamp: Long = 0L
        private var lastProcessedPowerAction: String? = null
        private var lastProcessedPowerTimestamp: Long = 0L
    }
}
