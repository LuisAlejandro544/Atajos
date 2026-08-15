package com.example.engine.triggers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AutomationEntity
import com.example.data.model.ShortcutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Asistente modular para programar y cancelar alarmas exactas con AlarmManager
 * para disparadores de atajos directos y automatizaciones basadas en hora del día.
 */
object TimeSchedulerHelper {

    private const val TAG = "TimeSchedulerHelper"
    private const val SHORTCUT_REQUEST_CODE_OFFSET = 100000

    fun parseHourMinute(timeStr: String): Pair<Int, Int>? {
        if (!timeStr.contains(":")) return null
        val parts = timeStr.split(":")
        if (parts.size < 2) return null
        val hour = parts[0].trim().toIntOrNull() ?: return null
        val minute = parts[1].trim().toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return Pair(hour, minute)
    }

    fun formatTimeString(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun scheduleShortcutAlarm(context: Context, shortcut: ShortcutEntity) {
        if (!shortcut.trigger.startsWith("TIME_EXACT:")) return

        val timeStr = shortcut.trigger.substringAfter("TIME_EXACT:")
        val parts = timeStr.split(":")
        if (parts.size < 2) return

        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
            action = TimeTriggerReceiver.ACTION_TIME_TRIGGER
            putExtra(TimeTriggerReceiver.EXTRA_SHORTCUT_ID, shortcut.id)
            putExtra(TimeTriggerReceiver.EXTRA_AUTOMATION_TITLE, "Atajo Programado: ${shortcut.title}")
        }

        val requestCode = (shortcut.id + SHORTCUT_REQUEST_CODE_OFFSET).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarma programada para el atajo ${shortcut.id} ('${shortcut.title}') a las $hour:$minute")
        } catch (e: Exception) {
            Log.e(TAG, "Error programando alarma para el atajo ${shortcut.id}", e)
        }
    }

    fun cancelShortcutAlarm(context: Context, shortcutId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
            action = TimeTriggerReceiver.ACTION_TIME_TRIGGER
        }
        val requestCode = (shortcutId + SHORTCUT_REQUEST_CODE_OFFSET).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarma cancelada para el atajo $shortcutId")
        }
    }

    fun scheduleAutomationAlarm(context: Context, automation: AutomationEntity) {
        if (!automation.isEnabled || automation.triggerValue.isBlank()) return

        val parts = automation.triggerValue.split(":")
        if (parts.size < 2) return

        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
            action = TimeTriggerReceiver.ACTION_TIME_TRIGGER
            putExtra(TimeTriggerReceiver.EXTRA_AUTOMATION_ID, automation.id)
            putExtra(TimeTriggerReceiver.EXTRA_SHORTCUT_ID, automation.shortcutId)
            putExtra(TimeTriggerReceiver.EXTRA_AUTOMATION_TITLE, automation.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            automation.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarma programada para la automatización ${automation.id} a las $hour:$minute")
        } catch (e: Exception) {
            Log.e(TAG, "Error programando alarma para la automatización ${automation.id}", e)
        }
    }

    fun cancelAutomationAlarm(context: Context, automationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
            action = TimeTriggerReceiver.ACTION_TIME_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            automationId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarma cancelada para la automatización $automationId")
        }
    }

    fun rescheduleAll(context: Context) {
        val appContext = context.applicationContext
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val db = com.example.data.db.AppDatabase.getInstance(appContext)
                val automations = db.automationDao().getAllAutomationsSync()
                for (automation in automations) {
                    if (automation.isEnabled && automation.triggerType == com.example.data.model.TriggerType.TIME_OF_DAY) {
                        scheduleAutomationAlarm(appContext, automation)
                    }
                }

                val shortcuts = db.shortcutDao().getAllShortcutsSync()
                for (shortcut in shortcuts) {
                    if (shortcut.trigger.startsWith("TIME_EXACT:")) {
                        scheduleShortcutAlarm(appContext, shortcut)
                    }
                }
                Log.d(TAG, "Todas las alarmas y temporizadores se han reprogramado correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error reprogramando todas las alarmas", e)
            }
        }
    }
}
