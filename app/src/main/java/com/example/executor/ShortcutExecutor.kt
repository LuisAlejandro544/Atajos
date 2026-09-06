package com.example.executor

import android.content.Context
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity
import com.example.executor.handlers.AudioActionHandler
import com.example.executor.handlers.CommunicationActionHandler
import com.example.executor.handlers.DeviceActionHandler
import com.example.executor.handlers.NavigationActionHandler
import com.example.executor.handlers.TtsManager

data class ExecutionResult(
    val success: Boolean,
    val message: String
)

class ShortcutExecutor(private val context: Context) {

    private val deviceHandler = DeviceActionHandler(context)
    private val audioHandler = AudioActionHandler(context)
    private val navigationHandler = NavigationActionHandler(context)
    private val communicationHandler = CommunicationActionHandler(context)
    private val ttsManager = TtsManager(context)

    private val luaEngine by lazy { LuaShortcutEngine(context, this) }

    companion object {
        const val STEP_DELAY_MS = 1003L
    }

    fun executeSingleBlock(actionTypeStr: String, parameter: String): ExecutionResult {
        deviceHandler.triggerHapticFeedback()

        return try {
            when (actionTypeStr) {
                ActionType.FLASHLIGHT.name -> deviceHandler.toggleFlashlight()
                ActionType.OPEN_APP.name -> navigationHandler.openApp(parameter)
                ActionType.OPEN_URL.name -> navigationHandler.openUrl(parameter)
                ActionType.SET_VOLUME.name -> audioHandler.setVolume(parameter)
                ActionType.COPY_TEXT.name -> communicationHandler.copyToClipboard(parameter)
                ActionType.MAP_NAV.name -> navigationHandler.openMapNavigation(parameter)
                ActionType.SET_TIMER.name -> communicationHandler.setTimer(parameter)
                ActionType.SEND_MESSAGE.name -> communicationHandler.sendMessage(parameter)
                ActionType.SOUND_SETTINGS.name -> audioHandler.openSoundSettings()
                ActionType.SHARE_TEXT.name -> communicationHandler.shareText(parameter)
                ActionType.SPEAK.name -> speakText(parameter)
                ActionType.NOTIFICATION.name -> showNotification(parameter)
                ActionType.SET_BRIGHTNESS.name -> deviceHandler.setBrightness(parameter)
                ActionType.WAIT.name -> executeWait(parameter)
                ActionType.LUA_SCRIPT.name -> luaEngine.executeScript(parameter)
                else -> ExecutionResult(false, "Acción desconocida")
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Error al ejecutar: ${e.localizedMessage ?: "Fallo desconocido"}")
        }
    }

    fun speakText(
        text: String,
        engineOverride: com.example.tts.TtsEngineType? = null,
        voiceOverride: String? = null
    ): ExecutionResult {
        return ttsManager.speak(text, engineOverride, voiceOverride)
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
}
