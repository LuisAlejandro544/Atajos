package com.example.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.TriggerType
import com.example.executor.NotificationHelper
import com.example.executor.ShortcutExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PowerTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val triggerType = when (action) {
            Intent.ACTION_POWER_CONNECTED -> TriggerType.CHARGER_CONNECTED.name
            Intent.ACTION_POWER_DISCONNECTED -> TriggerType.CHARGER_DISCONNECTED.name
            else -> return
        }

        val eventLabel = if (triggerType == TriggerType.CHARGER_CONNECTED.name) "Cargador conectado" else "Cargador desconectado"
        Log.i("PowerTriggerReceiver", "Detectado evento de energía: $eventLabel")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val shortcuts = db.shortcutDao().getShortcutsByTrigger(triggerType)
                if (shortcuts.isNotEmpty()) {
                    val executor = ShortcutExecutor(context)
                    for (shortcut in shortcuts) {
                        Log.i("PowerTriggerReceiver", "Disparando atajo automático: ${shortcut.title}")
                        db.shortcutDao().incrementExecutionCount(shortcut.id)
                        val blocks = executor.resolveBlocks(shortcut)
                        for (block in blocks) {
                            executor.executeSingleBlock(block.actionType, block.parameter)
                        }
                        NotificationHelper.showNotification(
                            context = context,
                            title = "⚡ Disparador Activo ($eventLabel)",
                            message = "Se ejecutó el atajo: \"${shortcut.title}\"",
                            soundType = NotificationHelper.SOUND_POP
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PowerTriggerReceiver", "Error procesando disparador de energía: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
