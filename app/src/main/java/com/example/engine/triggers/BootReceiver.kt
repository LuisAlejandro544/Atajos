package com.example.engine.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.TriggerType
import com.example.engine.service.AutomationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("BootReceiver", "Evento de inicio recibido: $action")
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val appContext = context.applicationContext
            AutomationService.start(appContext)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(appContext)
                    val automations = db.automationDao().getAllAutomationsSync()
                    automations.filter { it.isEnabled && it.triggerType == TriggerType.TIME_OF_DAY }.forEach {
                        TimeSchedulerHelper.scheduleAutomationAlarm(appContext, it)
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error reprogramando alarmas tras reinicio", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
