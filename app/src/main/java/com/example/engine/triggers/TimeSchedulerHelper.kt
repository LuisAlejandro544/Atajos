package com.example.engine.triggers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AutomationEntity
import java.util.Calendar

/**
 * Asistente modular para programar y cancelar alarmas exactas con AlarmManager
 * para disparadores de automatizaciones basadas en hora del día (TIME_OF_DAY).
 */
object TimeSchedulerHelper {

    private const val TAG = "TimeSchedulerHelper"

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
}
