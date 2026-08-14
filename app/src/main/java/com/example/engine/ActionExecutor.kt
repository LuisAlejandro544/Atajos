package com.example.engine

import android.content.Context
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.engine.handlers.ActionHandler
import com.example.engine.handlers.AppLauncherActionHandler
import com.example.engine.handlers.BrightnessActionHandler
import com.example.engine.handlers.CameraActionHandler
import com.example.engine.handlers.FlashlightActionHandler
import com.example.engine.handlers.NotificationActionHandler
import com.example.engine.handlers.SystemIntentsActionHandler
import com.example.engine.handlers.TtsActionHandler
import com.example.engine.handlers.UtilityActionHandler
import com.example.engine.handlers.VibrationActionHandler
import com.example.engine.handlers.VolumeActionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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

/**
 * Orquestador principal de ejecución de atajos y automatizaciones.
 * Despacha cada paso al manejador especializado correspondiente implementando
 * el patrón Strategy / Command.
 */
class ActionExecutor(context: Context) {

    private val _status = MutableStateFlow(ExecutionStatus())
    val status: StateFlow<ExecutionStatus> = _status.asStateFlow()

    private val handlers: List<ActionHandler> = listOf(
        TtsActionHandler(context),
        FlashlightActionHandler(context),
        VibrationActionHandler(context),
        NotificationActionHandler(context),
        SystemIntentsActionHandler(context),
        UtilityActionHandler(context),
        AppLauncherActionHandler(context),
        BrightnessActionHandler(context),
        VolumeActionHandler(context),
        CameraActionHandler(context)
    )

    private val handlerMap: Map<ActionType, ActionHandler> = buildMap {
        for (handler in handlers) {
            for (type in handler.supportedTypes) {
                put(type, handler)
            }
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
                lastMessage = "Error en ${action.title}: ${e.localizedMessage ?: "Fallo inesperado"}"
                break
            }

            // Pausa estética breve entre pasos para retroalimentación visual en la UI
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
        val handler = handlerMap[action.type]
            ?: throw IllegalStateException("No hay manejador registrado para ${action.type}")
        handler.execute(action)
    }

    fun dismissStatus() {
        _status.value = ExecutionStatus()
    }

    fun release() {
        for (handler in handlers) {
            try {
                handler.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
