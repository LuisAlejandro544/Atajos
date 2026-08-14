package com.example.engine.handlers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

class NotificationActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.SHOW_NOTIFICATION)

    private val CHANNEL_ID = "atajos_automation_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Atajos"
            val descriptionText = "Canal de avisos y alertas para automatizaciones de Atajos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager? =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override suspend fun execute(action: ShortcutAction): String {
        val rawTitle = action.param1.ifBlank { "Atajos" }
        val rawMessage = action.param2.ifBlank { "Acción de automatización completada" }

        val title = com.example.engine.VariableResolverHelper.resolve(rawTitle, context)
        val message = com.example.engine.VariableResolverHelper.resolve(rawMessage, context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        return "Notificación enviada: $title"
    }
}
