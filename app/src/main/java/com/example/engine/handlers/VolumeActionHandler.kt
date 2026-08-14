package com.example.engine.handlers

import android.content.Context
import android.media.AudioManager
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import kotlin.math.roundToInt

/**
 * Manejador especializado para el control y ajuste de sonido y volumen del dispositivo.
 * Permite subir, bajar, silenciar o fijar un porcentaje en canales multimedia, notificaciones, tonos y alarmas.
 */
class VolumeActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.SET_VOLUME)

    override suspend fun execute(action: ShortcutAction): String {
        val streamName = action.param1.trim().lowercase().ifBlank { "music" }
        val modeOrValue = action.param2.trim().lowercase().ifBlank { "raise" }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return "No se pudo acceder al servicio de audio"

        val (streamType, streamLabel) = when (streamName) {
            "ring", "llamadas", "tono" -> Pair(AudioManager.STREAM_RING, "Tono de llamada")
            "notification", "notificaciones", "avisos" -> Pair(AudioManager.STREAM_NOTIFICATION, "Notificaciones")
            "alarm", "alarma", "reloj" -> Pair(AudioManager.STREAM_ALARM, "Alarma")
            "system", "sistema" -> Pair(AudioManager.STREAM_SYSTEM, "Sistema")
            else -> Pair(AudioManager.STREAM_MUSIC, "Multimedia")
        }

        return try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val currentVolume = audioManager.getStreamVolume(streamType)

            when (modeOrValue) {
                "raise", "subir", "+", "aumentar" -> {
                    audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    val newVol = audioManager.getStreamVolume(streamType)
                    val pct = ((newVol / maxVolume.toFloat()) * 100).roundToInt()
                    "Volumen de $streamLabel aumentado ($pct%)"
                }

                "lower", "bajar", "-", "disminuir" -> {
                    audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    val newVol = audioManager.getStreamVolume(streamType)
                    val pct = ((newVol / maxVolume.toFloat()) * 100).roundToInt()
                    "Volumen de $streamLabel reducido ($pct%)"
                }

                "mute", "silencio", "0" -> {
                    audioManager.setStreamVolume(streamType, 0, AudioManager.FLAG_SHOW_UI)
                    "Volumen de $streamLabel silenciado"
                }

                "max", "maximo", "100" -> {
                    audioManager.setStreamVolume(streamType, maxVolume, AudioManager.FLAG_SHOW_UI)
                    "Volumen de $streamLabel al 100%"
                }

                else -> {
                    val pct = modeOrValue.toIntOrNull()?.coerceIn(0, 100) ?: 50
                    val targetVolume = ((pct / 100f) * maxVolume).roundToInt().coerceIn(0, maxVolume)
                    audioManager.setStreamVolume(streamType, targetVolume, AudioManager.FLAG_SHOW_UI)
                    "Volumen de $streamLabel fijado al $pct%"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al ajustar volumen: ${e.localizedMessage ?: "Restricción del sistema"}"
        }
    }
}
