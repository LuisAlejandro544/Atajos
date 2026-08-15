package com.example.engine.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.ActionJsonHelper
import com.example.data.model.ExecutionLogEntity
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver activado por AlarmManager a la hora exacta programada.
 * Ejecuta el atajo configurado en la automatización y reprograma la alarma para el día siguiente.
 */
class TimeTriggerReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TIME_TRIGGER = "com.example.action.TIME_TRIGGER"
        const val EXTRA_AUTOMATION_ID = "extra_automation_id"
        const val EXTRA_SHORTCUT_ID = "extra_shortcut_id"
        const val EXTRA_AUTOMATION_TITLE = "extra_automation_title"
        private const val TAG = "TimeTriggerReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIME_TRIGGER) return

        val automationId = intent.getLongExtra(EXTRA_AUTOMATION_ID, -1L)
        val shortcutId = intent.getLongExtra(EXTRA_SHORTCUT_ID, -1L)
        val automationTitle = intent.getStringExtra(EXTRA_AUTOMATION_TITLE) ?: "Automatización Horaria"

        if (automationId == -1L || shortcutId == -1L) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(appContext)
                val repository = ShortcutRepository(db)
                val automationsList = db.automationDao().getAllAutomationsSync()
                val currentAutomation = automationsList.find { it.id == automationId }

                if (currentAutomation != null && currentAutomation.isEnabled) {
                    val shortcut = repository.getShortcutById(shortcutId)
                    if (shortcut != null) {
                        val actions = ActionJsonHelper.fromJson(shortcut.actionsJson)
                        val executor = ActionExecutor(appContext)

                        executor.executeShortcut(
                            shortcutId = shortcut.id,
                            shortcutTitle = "${shortcut.title} (⏰ $automationTitle)",
                            actions = actions
                        ) { success, resultMessage, durationMs ->
                            CoroutineScope(Dispatchers.IO).launch {
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
                                        summary = "Disparado por horario: $automationTitle. $resultMessage"
                                    )
                                )
                                executor.release()
                            }
                        }

                        // Reprogramar para el día siguiente automáticamente
                        TimeSchedulerHelper.scheduleAutomationAlarm(appContext, currentAutomation)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ejecutando automatización horaria $automationId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
