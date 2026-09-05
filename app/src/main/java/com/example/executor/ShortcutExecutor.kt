package com.example.executor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.Settings
import android.speech.tts.TextToSpeech
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity
import java.util.Locale
import kotlin.math.roundToInt

data class ExecutionResult(
    val success: Boolean,
    val message: String
)

class ShortcutExecutor(private val context: Context) {

    private val luaEngine by lazy { LuaShortcutEngine(context, this) }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val pendingSpeechQueue = mutableListOf<String>()

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    try {
                        tts?.language = Locale.getDefault()
                    } catch (_: Exception) {}
                    // Process any queued speech
                    synchronized(pendingSpeechQueue) {
                        for (text in pendingSpeechQueue) {
                            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "shortcut_tts_${System.currentTimeMillis()}")
                        }
                        pendingSpeechQueue.clear()
                    }
                }
            }
        } catch (_: Exception) {
            isTtsInitialized = false
        }
    }

    companion object {
        private var isFlashlightOn = false
        const val STEP_DELAY_MS = 1003L
    }

    fun executeSingleBlock(actionTypeStr: String, parameter: String): ExecutionResult {
        triggerHapticFeedback()

        return try {
            when (actionTypeStr) {
                ActionType.FLASHLIGHT.name -> toggleFlashlight()
                ActionType.OPEN_APP.name -> openApp(parameter)
                ActionType.OPEN_URL.name -> openUrl(parameter)
                ActionType.SET_VOLUME.name -> setVolume(parameter)
                ActionType.COPY_TEXT.name -> copyToClipboard(parameter)
                ActionType.MAP_NAV.name -> openMapNavigation(parameter)
                ActionType.SET_TIMER.name -> setTimer(parameter)
                ActionType.SEND_MESSAGE.name -> sendMessage(parameter)
                ActionType.SOUND_SETTINGS.name -> openSoundSettings()
                ActionType.SHARE_TEXT.name -> shareText(parameter)
                ActionType.SPEAK.name -> speakText(parameter)
                ActionType.NOTIFICATION.name -> showNotification(parameter)
                ActionType.SET_BRIGHTNESS.name -> setBrightness(parameter)
                ActionType.WAIT.name -> executeWait(parameter)
                ActionType.LUA_SCRIPT.name -> luaEngine.executeScript(parameter)
                else -> ExecutionResult(false, "Acción desconocida")
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Error al ejecutar: ${e.localizedMessage ?: "Fallo desconocido"}")
        }
    }

    private fun executeWait(parameter: String): ExecutionResult {
        val ms = parameter.trim().toLongOrNull()?.coerceAtLeast(0L) ?: STEP_DELAY_MS
        return ExecutionResult(true, "Espera de ${ms} ms")
    }

    fun resolveBlocks(shortcut: ShortcutEntity): List<ActionBlock> {
        return if (shortcut.actions.isNotEmpty()) {
            shortcut.actions
        } else {
            listOf(
                ActionBlock(
                    actionType = shortcut.actionType,
                    parameter = shortcut.parameter,
                    customLabel = shortcut.description
                )
            )
        }
    }

    private fun toggleFlashlight(): ExecutionResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return ExecutionResult(false, "Cámara no disponible")

        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }

            if (cameraId == null) {
                return ExecutionResult(false, "No se encontró flash")
            }

            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)
            val state = if (isFlashlightOn) "encendida" else "apagada"
            ExecutionResult(true, "Linterna $state")
        } catch (e: CameraAccessException) {
            ExecutionResult(false, "Flash inaccesible: ${e.message}")
        } catch (e: Exception) {
            ExecutionResult(false, "Error con linterna: ${e.message}")
        }
    }

    private fun openApp(packageNameParam: String): ExecutionResult {
        val pkg = packageNameParam.trim()
        if (pkg.isEmpty()) {
            return ExecutionResult(false, "No se seleccionó ninguna aplicación")
        }
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(pkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent != null) {
                context.startActivity(intent)
                val label = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg
                }
                ExecutionResult(true, "Abriendo $label")
            } else {
                ExecutionResult(false, "App no disponible o desinstalada: $pkg")
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Error al abrir app: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    private fun openUrl(urlParam: String): ExecutionResult {
        var url = urlParam.trim()
        if (url.isEmpty()) {
            url = "https://www.google.com"
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Abriendo $url")
    }

    private fun copyToClipboard(text: String): ExecutionResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return ExecutionResult(false, "Portapapeles no disponible")

        val clip = ClipData.newPlainText("Atajo", text)
        clipboard.setPrimaryClip(clip)
        val preview = if (text.length > 28) text.take(25) + "..." else text
        return ExecutionResult(true, "Copiado: \"$preview\"")
    }

    private fun openMapNavigation(destination: String): ExecutionResult {
        val query = destination.trim().ifEmpty { "Casa" }
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Ruta hacia: $query")
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ExecutionResult(true, "Buscando en mapas: $query")
        }
    }

    private fun setTimer(minutesParam: String): ExecutionResult {
        val minutes = minutesParam.trim().toIntOrNull() ?: 5
        val seconds = (minutes * 60).coerceAtLeast(1)

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Atajo ($minutes min)")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Temporizador de $minutes min iniciado")
        } catch (_: Exception) {
            ExecutionResult(false, "App de reloj no disponible")
        }
    }

    private fun sendMessage(messageText: String): ExecutionResult {
        val text = messageText.trim().ifEmpty { "Hola" }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Enviar mensaje rápido").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ExecutionResult(true, "Preparando envío de mensaje")
    }

    private fun openSoundSettings(): ExecutionResult {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Ajustes de sonido abiertos")
    }

    private fun setVolume(percentParam: String): ExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ExecutionResult(false, "Control de volumen no disponible")

        val cleanParam = percentParam.trim().removeSuffix("%")
        val percent = cleanParam.toIntOrNull()?.coerceIn(0, 100) ?: 70
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = ((percent / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)

        return try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
            ExecutionResult(true, "Volumen ajustado al $percent%")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al configurar volumen: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    private fun shareText(text: String): ExecutionResult {
        val content = text.trim().ifEmpty { "Texto compartido" }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Compartir con").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ExecutionResult(true, "Compartiendo texto")
    }

    fun speakText(text: String): ExecutionResult {
        val rawMessage = text.trim().ifEmpty { "Atajo ejecutado" }
        val messageToSpeak = VariableResolver.resolve(rawMessage, context)
        return try {
            if (tts == null) {
                ExecutionResult(false, "Servicio de Texto a Voz no disponible en el sistema")
            } else if (!isTtsInitialized) {
                synchronized(pendingSpeechQueue) {
                    pendingSpeechQueue.add(messageToSpeak)
                }
                ExecutionResult(true, "Voz en inicialización: texto puesto en cola")
            } else {
                val utteranceId = "shortcut_tts_${System.currentTimeMillis()}"
                val result = tts?.speak(messageToSpeak, TextToSpeech.QUEUE_ADD, null, utteranceId)
                if (result == TextToSpeech.ERROR) {
                    ExecutionResult(false, "Error al emitir voz con el motor del sistema")
                } else {
                    ExecutionResult(true, "Reproduciendo voz: \"${messageToSpeak.take(30)}${if (messageToSpeak.length > 30) "..." else ""}\"")
                }
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Excepción al reproducir voz: ${e.localizedMessage ?: "Error"}")
        }
    }

    private fun showNotification(parameter: String): ExecutionResult {
        val (soundType, rawTemplate) = if (parameter.startsWith("sound:")) {
            val pipeIndex = parameter.indexOf('|')
            if (pipeIndex != -1) {
                val sound = parameter.substring(6, pipeIndex).trim()
                val text = parameter.substring(pipeIndex + 1)
                Pair(sound, text)
            } else {
                Pair(NotificationHelper.SOUND_DEFAULT, parameter)
            }
        } else {
            Pair(NotificationHelper.SOUND_DEFAULT, parameter)
        }

        val template = rawTemplate.trim().ifEmpty { "Atajo completado a las {hora} | Batería: {bateria}%" }
        val resolved = VariableResolver.resolve(template, context)
        val success = NotificationHelper.showNotification(
            context = context,
            title = "Atajo Ejecutado",
            message = resolved,
            soundType = soundType
        )
        return if (success) {
            val preview = if (resolved.length > 30) resolved.take(28) + "..." else resolved
            val soundLabel = if (soundType == NotificationHelper.SOUND_POP) " [Pop]" else ""
            ExecutionResult(true, "Notificación$soundLabel: \"$preview\"")
        } else {
            ExecutionResult(false, "No se pudo mostrar la notificación (verifica permisos del sistema)")
        }
    }

    private fun setBrightness(parameter: String): ExecutionResult {
        val clean = parameter.trim().removeSuffix("%")
        val percent = clean.toIntOrNull()?.coerceIn(0, 100) ?: 80

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
                return ExecutionResult(
                    false,
                    "Permiso requerido: concede permiso en la pantalla de ajustes de brillo."
                )
            }
        }

        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            val brightness255 = ((percent / 100f) * 255).roundToInt().coerceIn(1, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness255
            )
            ExecutionResult(true, "Brillo ajustado al $percent%")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al ajustar brillo: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {
            // ignore
        }
    }
}
