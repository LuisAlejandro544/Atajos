package com.example.executor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_ID_DEFAULT = "shortcuts_priority_channel"
    const val CHANNEL_ID_POP = "shortcuts_priority_channel_pop"

    const val CHANNEL_NAME_DEFAULT = "Atajos Prioritarios (Sonido Sistema)"
    const val CHANNEL_NAME_POP = "Atajos Prioritarios (Pop Notification)"

    const val SOUND_DEFAULT = "default"
    const val SOUND_POP = "pop"

    private var activePreviewPlayer: MediaPlayer? = null

    fun getPopSoundUri(context: Context): Uri {
        val resId = context.resources.getIdentifier("pop_notification", "raw", context.packageName)
        return if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun getSoundUri(context: Context, soundType: String): Uri {
        return if (soundType == SOUND_POP) {
            getPopSoundUri(context)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Canal con sonido predeterminado del sistema (con bypass DND)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channelDefault = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                CHANNEL_NAME_DEFAULT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones urgentes con sonido estándar del teléfono"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(defaultSoundUri, audioAttributes)
                setBypassDnd(true)
            }
            nm.createNotificationChannel(channelDefault)

            // 2. Canal con sonido Pop Notification de GabrielAraujo CC0 (con bypass DND)
            val popSoundUri = getPopSoundUri(context)
            val channelPop = NotificationChannel(
                CHANNEL_ID_POP,
                CHANNEL_NAME_POP,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones urgentes con sonido Pop Notification"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(popSoundUri, audioAttributes)
                setBypassDnd(true)
            }
            nm.createNotificationChannel(channelPop)
        }
    }

    fun previewSound(context: Context, soundType: String) {
        try {
            activePreviewPlayer?.stop()
            activePreviewPlayer?.release()
            activePreviewPlayer = null

            val uri = getSoundUri(context, soundType)
            val player = MediaPlayer.create(context, uri) ?: return
            activePreviewPlayer = player
            player.setOnCompletionListener {
                it.release()
                if (activePreviewPlayer == it) {
                    activePreviewPlayer = null
                }
            }
            player.start()
        } catch (_: Exception) {
            // Manejo seguro silencioso
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        soundType: String = SOUND_DEFAULT,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ): Boolean {
        initChannels(context)

        val targetChannelId = if (soundType == SOUND_POP) CHANNEL_ID_POP else CHANNEL_ID_DEFAULT
        val soundUri = getSoundUri(context, soundType)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, targetChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .setContentIntent(pendingIntent)

        return try {
            val nm = NotificationManagerCompat.from(context)
            nm.notify(notificationId, builder.build())
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }
}
