package com.example.engine.handlers

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class TtsActionHandler(private val context: Context) : ActionHandler, TextToSpeech.OnInitListener {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.SPEAK_TEXT)

    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady = false
    private var initDeferred = CompletableDeferred<Boolean>()

    init {
        initTts()
    }

    private fun initTts() {
        Handler(Looper.getMainLooper()).post {
            try {
                if (initDeferred.isCompleted) {
                    initDeferred = CompletableDeferred()
                }
                isTtsReady = false
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                Log.e(TAG, "Error instanciando TextToSpeech en Main Looper", e)
                if (!initDeferred.isCompleted) {
                    initDeferred.complete(false)
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val currentTts = tts
                if (currentTts != null) {
                    // Configurar atributos de audio para que suene claro incluso con pantalla bloqueada o en silencio
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    currentTts.setAudioAttributes(audioAttributes)

                    val result = currentTts.setLanguage(Locale("es", "ES"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        currentTts.setLanguage(Locale.getDefault())
                    }
                }
                isTtsReady = true
                if (!initDeferred.isCompleted) {
                    initDeferred.complete(true)
                }
                Log.d(TAG, "TextToSpeech inicializado con éxito y listo para reproducir")
            } catch (e: Exception) {
                Log.e(TAG, "Error configurando idioma/audio en TTS", e)
                if (!initDeferred.isCompleted) {
                    initDeferred.complete(false)
                }
            }
        } else {
            Log.e(TAG, "Fallo al inicializar TTS con status: $status")
            isTtsReady = false
            if (!initDeferred.isCompleted) {
                initDeferred.complete(false)
            }
        }
    }

    override suspend fun execute(action: ShortcutAction): String {
        val rawText = action.param1.ifBlank {
            if (action.param2.equals("read_notification", ignoreCase = true)) {
                "{ULTIMA_NOTIFICACION}"
            } else {
                "Atajo ejecutado correctamente"
            }
        }
        val text = com.example.engine.VariableResolverHelper.resolve(rawText, context)

        // Si TTS no está listo o fue liberado/desconectado por el sistema, reinicializar bajo demanda
        if (tts == null || !isTtsReady) {
            Log.d(TAG, "TextToSpeech no estaba listo al ejecutar acción. Reinicializando bajo demanda...")
            initTts()
            withTimeoutOrNull(2500L) {
                initDeferred.await()
            }
        }

        var speechSuccess = false
        val currentTts = tts

        if (currentTts != null && isTtsReady) {
            val utteranceId = "atajos_tts_${System.currentTimeMillis()}"
            val speechDeferred = CompletableDeferred<Unit>()

            val speakResult = withContext(Dispatchers.Main) {
                try {
                    currentTts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {
                            Log.d(TAG, "Inicio de pronunciación TTS: $id")
                        }
                        override fun onDone(id: String?) {
                            Log.d(TAG, "Fin de pronunciación TTS: $id")
                            if (id == utteranceId && !speechDeferred.isCompleted) {
                                speechDeferred.complete(Unit)
                            }
                        }
                        override fun onError(id: String?) {
                            Log.w(TAG, "Error en pronunciación TTS: $id")
                            if (id == utteranceId && !speechDeferred.isCompleted) {
                                speechDeferred.complete(Unit)
                            }
                        }
                    })

                    val params = Bundle().apply {
                        putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
                        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                    }
                    currentTts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                } catch (e: Exception) {
                    Log.e(TAG, "Excepción invocando tts.speak", e)
                    TextToSpeech.ERROR
                }
            }

            if (speakResult == TextToSpeech.SUCCESS) {
                speechSuccess = true
                // Damos tiempo a reproducir el audio completo antes de finalizar el hilo o receptor
                withTimeoutOrNull(6000L) {
                    speechDeferred.await()
                }
            } else {
                Log.w(TAG, "tts.speak devolvió código de error ($speakResult). Intentando fallback sonoro y reconexión...")
                isTtsReady = false
                initTts() // Preparar para el siguiente evento
            }
        }

        return if (speechSuccess) {
            "Pronunciando: \"$text\""
        } else {
            // Fallback sonoro del sistema si el motor TTS tarda o falló la conexión IPC
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
                ringtone?.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error reproduciendo tono de fallback", e)
            }

            // Fallback en hilo principal para el Toast
            Handler(Looper.getMainLooper()).post {
                try {
                    Toast.makeText(context.applicationContext, "Atajo: $text", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error mostrando Toast de fallback", e)
                }
            }
            "Aviso de voz: \"$text\""
        }
    }

    override fun onCancelled() {
        try {
            Handler(Looper.getMainLooper()).post {
                try {
                    tts?.stop()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        try {
            Handler(Looper.getMainLooper()).post {
                try {
                    tts?.stop()
                    tts?.shutdown()
                    tts = null
                    isTtsReady = false
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "TtsActionHandler"
    }
}
