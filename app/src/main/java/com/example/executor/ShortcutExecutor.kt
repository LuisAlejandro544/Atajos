package com.example.executor

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
import android.provider.Settings
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity

data class ExecutionResult(
    val success: Boolean,
    val message: String
)

class ShortcutExecutor(private val context: Context) {

    private val luaEngine by lazy { LuaShortcutEngine(context, this) }

    companion object {
        private var isFlashlightOn = false
        const val STEP_DELAY_MS = 1003L
    }

    fun executeSingleBlock(actionTypeStr: String, parameter: String): ExecutionResult {
        triggerHapticFeedback()

        return try {
            when (actionTypeStr) {
                ActionType.FLASHLIGHT.name -> toggleFlashlight()
                ActionType.OPEN_URL.name -> openUrl(parameter)
                ActionType.COPY_TEXT.name -> copyToClipboard(parameter)
                ActionType.MAP_NAV.name -> openMapNavigation(parameter)
                ActionType.SET_TIMER.name -> setTimer(parameter)
                ActionType.SEND_MESSAGE.name -> sendMessage(parameter)
                ActionType.SOUND_SETTINGS.name -> openSoundSettings()
                ActionType.SHARE_TEXT.name -> shareText(parameter)
                ActionType.LUA_SCRIPT.name -> luaEngine.executeScript(parameter)
                else -> ExecutionResult(false, "Acción desconocida")
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Error al ejecutar: ${e.localizedMessage ?: "Fallo desconocido"}")
        }
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
