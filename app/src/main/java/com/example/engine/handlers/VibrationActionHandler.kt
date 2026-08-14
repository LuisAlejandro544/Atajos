package com.example.engine.handlers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

/**
 * Manejador especializado para efectos y patrones hápticos avanzados.
 * Soporta efectos predefinidos de Material/Android 10+, patrones rítmicos y duraciones personalizadas.
 */
class VibrationActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.VIBRATE)

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override suspend fun execute(action: ShortcutAction): String {
        val pattern = action.param2.ifBlank { "heavy" }.lowercase()
        val customDuration = action.param1.toLongOrNull() ?: 250L

        val vibrator = getVibrator()
        if (vibrator == null || !vibrator.hasVibrator()) {
            return "Dispositivo sin motor de vibración"
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (pattern) {
                    "click", "tap" -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    "heavy", "strong" -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    }
                    "double" -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                    }
                    "heartbeat" -> {
                        val timings = longArrayOf(0, 120, 100, 240)
                        val amplitudes = intArrayOf(0, 180, 0, 255)
                        if (vibrator.hasAmplitudeControl()) {
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                        } else {
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                        }
                    }
                    "alert", "burst" -> {
                        val timings = longArrayOf(0, 80, 60, 80, 60, 80, 60, 160)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                    "sos" -> {
                        val timings = longArrayOf(0, 100, 100, 100, 100, 100, 250, 250, 100, 250, 100, 250, 250, 100, 100, 100, 100, 100)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                    else -> {
                        // Custom duration
                        vibrator.vibrate(VibrationEffect.createOneShot(customDuration.coerceIn(20L, 5000L), VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (pattern) {
                    "click", "tap" -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    "double" -> {
                        val timings = longArrayOf(0, 80, 70, 100)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                    "heartbeat" -> {
                        val timings = longArrayOf(0, 120, 100, 240)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                    "alert", "burst" -> {
                        val timings = longArrayOf(0, 80, 60, 80, 60, 80, 60, 160)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                    "sos" -> {
                        val timings = longArrayOf(0, 100, 100, 100, 100, 100, 250, 250, 100, 250, 100, 250, 250, 100, 100, 100, 100, 100)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                    else -> {
                        val duration = if (pattern == "heavy") 300L else customDuration.coerceIn(20L, 5000L)
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (pattern) {
                    "click", "tap" -> vibrator.vibrate(50L)
                    "double" -> vibrator.vibrate(longArrayOf(0, 80, 70, 100), -1)
                    "heartbeat" -> vibrator.vibrate(longArrayOf(0, 120, 100, 240), -1)
                    "alert", "burst" -> vibrator.vibrate(longArrayOf(0, 80, 60, 80, 60, 80, 60, 160), -1)
                    "sos" -> vibrator.vibrate(longArrayOf(0, 100, 100, 100, 100, 100, 250, 250, 100, 250, 100, 250, 250, 100, 100, 100, 100, 100), -1)
                    else -> vibrator.vibrate(if (pattern == "heavy") 300L else customDuration.coerceIn(20L, 5000L))
                }
            }
            return "Vibración háptica activada: $pattern"
        } catch (e: Exception) {
            return "Error al vibrar: ${e.message}"
        }
    }

    override fun onCancelled() {
        try {
            getVibrator()?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        try {
            getVibrator()?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
