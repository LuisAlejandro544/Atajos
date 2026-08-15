package com.example.engine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.triggers.PowerTriggerReceiver

class AutomationService : Service() {

    private var powerReceiver: PowerTriggerReceiver? = null
    private var isReceiverRegistered = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Iniciando AutomationService...")
        createNotificationChannel()
        startForegroundServiceNotification()
        registerHardwareReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AutomationService onStartCommand ejecutado")
        if (!isReceiverRegistered) {
            registerHardwareReceivers()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Automatizaciones",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitorea eventos de hardware y disparadores de atajos en tiempo real"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Atajos activo")
            .setContentText("Monitoreando disparadores de batería y sistema")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        0
                    )
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando Foreground Service", e)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (ex: Exception) {
                Log.e(TAG, "Fallo fallback startForeground", ex)
            }
        }
    }

    private fun registerHardwareReceivers() {
        if (isReceiverRegistered) return
        try {
            powerReceiver = PowerTriggerReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction("android.intent.action.POWER_CONNECTED")
                addAction("android.intent.action.POWER_DISCONNECTED")
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_BATTERY_OKAY)
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }

            ContextCompat.registerReceiver(
                this,
                powerReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isReceiverRegistered = true
            Log.d(TAG, "Receptor de disparadores de hardware registrado dinámicamente con éxito")
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando receptores dinámicos", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered && powerReceiver != null) {
            try {
                unregisterReceiver(powerReceiver)
                isReceiverRegistered = false
                Log.d(TAG, "Receptor dinámico desregistrado")
            } catch (e: Exception) {
                Log.e(TAG, "Error desregistrando receptor", e)
            }
        }
        Log.d(TAG, "AutomationService destruido")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AutomationService"
        private const val CHANNEL_ID = "atajos_automation_channel"
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            try {
                val intent = Intent(context, AutomationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error invocando start en AutomationService", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, AutomationService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error invocando stop en AutomationService", e)
            }
        }
    }
}
