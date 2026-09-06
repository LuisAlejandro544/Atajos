package com.example.executor.handlers

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import com.example.executor.ExecutionResult
import kotlin.math.roundToInt

class AudioActionHandler(private val context: Context) {

    fun setVolume(percentParam: String): ExecutionResult {
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

    fun openSoundSettings(): ExecutionResult {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Ajustes de sonido abiertos")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al abrir ajustes de sonido: ${e.localizedMessage ?: "Fallo"}")
        }
    }
}
