package com.example.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.Locale

data class ExecutionStatus(
    val isRunning: Boolean = false,
    val shortcutId: Long = 0L,
    val shortcutTitle: String = "",
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val currentActionTitle: String = "",
    val resultMessage: String = "",
    val isFinished: Boolean = false,
    val isSuccess: Boolean = true
)

class ActionExecutor(private val context: Context) : TextToSpeech.OnInitListener {

    private val _status = MutableStateFlow(ExecutionStatus())
    val status: StateFlow<ExecutionStatus> = _status.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isTorchOn = false

    private val CHANNEL_ID = "atajos_automation_channel"

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        createNotificationChannel()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            isTtsReady = true
        }
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

    suspend fun executeShortcut(
        shortcutId: Long,
        shortcutTitle: String,
        actions: List<ShortcutAction>,
        onFinished: (Boolean, String, Long) -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        if (actions.isEmpty()) {
            _status.value = ExecutionStatus(
                isRunning = false,
                shortcutId = shortcutId,
                shortcutTitle = shortcutTitle,
                resultMessage = "El atajo no contiene acciones",
                isFinished = true,
                isSuccess = false
            )
            onFinished(false, "Sin acciones", 0L)
            return
        }

        _status.value = ExecutionStatus(
            isRunning = true,
            shortcutId = shortcutId,
            shortcutTitle = shortcutTitle,
            currentStep = 0,
            totalSteps = actions.size,
            currentActionTitle = "Iniciando...",
            resultMessage = "Ejecutando..."
        )

        var allSuccessful = true
        var lastMessage = "Ejecutado con éxito"

        for (index in actions.indices) {
            val action = actions[index]
            _status.value = ExecutionStatus(
                isRunning = true,
                shortcutId = shortcutId,
                shortcutTitle = shortcutTitle,
                currentStep = index + 1,
                totalSteps = actions.size,
                currentActionTitle = action.title.ifBlank { action.type.displayName },
                resultMessage = "Paso ${index + 1} de ${actions.size}: ${action.type.displayName}"
            )

            try {
                val stepResult = executeSingleAction(action)
                if (stepResult.isNotBlank()) {
                    lastMessage = stepResult
                }
            } catch (e: Exception) {
                e.printStackTrace()
                allSuccessful = false
                lastMessage = "Error en ${action.title}: ${e.localizedMessage ?: "Fallo"}"
                break
            }

            // Small aesthetic pause between steps for visual feedback
            if (actions.size > 1 && action.type != ActionType.WAIT_DELAY) {
                delay(300)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        _status.value = ExecutionStatus(
            isRunning = false,
            shortcutId = shortcutId,
            shortcutTitle = shortcutTitle,
            currentStep = actions.size,
            totalSteps = actions.size,
            currentActionTitle = if (allSuccessful) "Completado" else "Error",
            resultMessage = lastMessage,
            isFinished = true,
            isSuccess = allSuccessful
        )

        onFinished(allSuccessful, lastMessage, duration)
    }

    private suspend fun executeSingleAction(action: ShortcutAction): String = withContext(Dispatchers.Main) {
        when (action.type) {
            ActionType.SPEAK_TEXT -> {
                val text = action.param1.ifBlank { "Atajo ejecutado correctamente" }
                if (tts != null) {
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "atajos_tts_${System.currentTimeMillis()}")
                } else {
                    Toast.makeText(context, "Voz: $text", Toast.LENGTH_SHORT).show()
                }
                "Pronunciando: \"$text\""
            }

            ActionType.TOGGLE_FLASHLIGHT -> {
                val mode = action.param1.lowercase()
                toggleFlashlight(mode)
            }

            ActionType.VIBRATE -> {
                val durationStr = action.param1.ifBlank { "200" }
                val pattern = action.param2.lowercase()
                vibrateDevice(durationStr, pattern)
                "Vibración emitida"
            }

            ActionType.SHOW_NOTIFICATION -> {
                val title = action.param1.ifBlank { "Atajos" }
                val message = action.param2.ifBlank { "Acción de automatización completada" }
                showNotification(title, message)
                "Notificación enviada: $title"
            }

            ActionType.OPEN_URL -> {
                var url = action.param1.ifBlank { "https://google.com" }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Abriendo sitio web: $url"
            }

            ActionType.SEARCH_WEB -> {
                val query = action.param1.ifBlank { "Android Shortcuts" }
                val searchUrl = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Buscando en Google: $query"
            }

            ActionType.COPY_CLIPBOARD -> {
                val textToCopy = action.param1.ifBlank { "Texto copiado desde Atajos" }
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("Atajos", textToCopy)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                "Texto copiado al portapapeles"
            }

            ActionType.SEND_WHATSAPP -> {
                val phone = action.param1.trim().replace("+", "").replace(" ", "").replace("-", "")
                val text = action.param2.ifBlank { "Hola, te escribo desde mis Atajos" }
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val uri = if (phone.isNotEmpty()) {
                    Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedText")
                } else {
                    Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
                }
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Abriendo WhatsApp"
            }

            ActionType.SEND_SMS -> {
                val phone = action.param1.trim()
                val text = action.param2.ifBlank { "Mensaje automático desde Atajos" }
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$phone")
                    putExtra("sms_body", text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Preparando SMS a $phone"
            }

            ActionType.SHARE_TEXT -> {
                val textToShare = action.param1.ifBlank { "Compartido desde Atajos de Android" }
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, textToShare)
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val shareIntent = Intent.createChooser(sendIntent, "Compartir con").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(shareIntent)
                "Menú compartir abierto"
            }

            ActionType.SET_TIMER -> {
                val seconds = action.param1.toIntOrNull() ?: 60
                val label = action.param2.ifBlank { "Temporizador Atajo" }
                try {
                    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, label)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    "Temporizador de ${seconds}s iniciado"
                } catch (e: Exception) {
                    Toast.makeText(context, "Temporizador de $seconds seg ($label)", Toast.LENGTH_LONG).show()
                    "Temporizador programado ($seconds seg)"
                }
            }

            ActionType.WAIT_DELAY -> {
                val seconds = action.param1.toIntOrNull() ?: 1
                delay(seconds * 1000L)
                "Pausa de ${seconds}s completada"
            }

            ActionType.QUICK_CALCULATOR -> {
                val amount = action.param1.toDoubleOrNull() ?: 50.0
                val percentage = action.param2.toDoubleOrNull() ?: 15.0
                val calculated = (amount * percentage) / 100.0
                val total = amount + calculated
                val resultText = "Cuenta: $$amount | Propina ($percentage%): $$calculated | Total: $$total"
                Toast.makeText(context, resultText, Toast.LENGTH_LONG).show()
                resultText
            }
        }
    }

    private fun toggleFlashlight(mode: String): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        if (cameraManager == null) return "Cámara no disponible"

        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull() ?: return "Sin flash"

            val targetState = when (mode) {
                "on" -> true
                "off" -> false
                else -> !isTorchOn
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, targetState)
                isTorchOn = targetState
                if (targetState) "Linterna encendida" else "Linterna apagada"
            } else {
                "Linterna no soportada en esta versión"
            }
        } catch (e: CameraAccessException) {
            "Error al acceder a linterna: ${e.message}"
        } catch (e: Exception) {
            "Linterna: ${e.message}"
        }
    }

    private fun vibrateDevice(durationStr: String, pattern: String) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        val duration = durationStr.toLongOrNull() ?: 150L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (pattern) {
                "sos" -> {
                    val timings = longArrayOf(0, 100, 100, 100, 100, 100, 200, 300, 100, 300, 100, 300, 200, 100, 100, 100, 100, 100)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
                "double" -> {
                    val timings = longArrayOf(0, 100, 80, 100)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
                else -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager?.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    fun dismissStatus() {
        _status.value = ExecutionStatus()
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
