package com.example.executor.handlers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.data.model.UserInteractionConfig
import com.example.executor.UserInteractionBridge
import com.example.executor.UserInteractionReceiver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

class UserInteractionNotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "shortcut_interactive_channel"
        private const val NOTIFICATION_ID = 9001
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Interacciones de Atajos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones interactivas para responder palabras clave antes de continuar atajos"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun showInteractionNotification(
        shortcutTitle: String,
        config: UserInteractionConfig
    ): Boolean {
        val interactionId = java.util.UUID.randomUUID().toString()
        val deferred = CompletableDeferred<Boolean>()
        UserInteractionBridge.register(interactionId, deferred)

        val cancelIntent = Intent(context, UserInteractionReceiver::class.java).apply {
            action = UserInteractionReceiver.ACTION_CANCEL
            putExtra(UserInteractionReceiver.EXTRA_INTERACTION_ID, interactionId)
            putExtra(UserInteractionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            interactionId.hashCode(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val remoteInput = RemoteInput.Builder(UserInteractionReceiver.KEY_TEXT_REPLY)
            .setLabel("Escribe palabra clave (ej: ${config.expectedKeyword})")
            .build()

        val replyIntent = Intent(context, UserInteractionReceiver::class.java).apply {
            action = UserInteractionReceiver.ACTION_REPLY
            putExtra(UserInteractionReceiver.EXTRA_INTERACTION_ID, interactionId)
            putExtra(UserInteractionReceiver.EXTRA_EXPECTED_KEYWORD, config.expectedKeyword)
            putExtra(UserInteractionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            interactionId.hashCode() + 1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Responder (${config.expectedKeyword})",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val cancelAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Cancelar",
            cancelPendingIntent
        ).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Atajo: $shortcutTitle")
            .setContentText("${config.promptTitle}: ${config.promptMessage} (Palabra clave: ${config.expectedKeyword})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${config.promptTitle}\n${config.promptMessage}\n\nIntroduce '${config.expectedKeyword}' en la respuesta para continuar.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .addAction(replyAction)
            .addAction(cancelAction)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            return false
        }

        val result = withTimeoutOrNull(60_000L) {
            deferred.await()
        } ?: false

        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        return result
    }
}
