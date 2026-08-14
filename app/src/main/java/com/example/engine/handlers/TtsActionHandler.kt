package com.example.engine.handlers

import android.content.Context
import android.media.RingtoneManager
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
    private val initDeferred = CompletableDeferred<Boolean>()

    init {
        // En Android, TextToSpeech DEBE inicializarse en el hilo principal (Main Looper)
        // para que pueda enlazar de inmediato con el servicio del sistema de voz (IPC)
        Handler(Looper.getMainLooper()).post {
            try {
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
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                isTtsReady = true
                if (!initDeferred.isCompleted) {
                    initDeferred.complete(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error configurando idioma TTS", e)
                if (!initDeferred.isCompleted) {
                    initDeferred.complete(false)
                }
            }
        } else {
            Log.e(TAG, "Fallo al inicializar TTS con status: $status")
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

        // Si el motor TTS aún se está inicializando (común al conectar cargador en segundo plano),
        // esperamos hasta 2500ms a que termine de conectarse con el servicio de voz.
        if (!isTtsReady) {
            withTimeoutOrNull(2500L) {
                initDeferred.await()
            }
        }

        return if (tts != null && isTtsReady) {
            val utteranceId = "atajos_tts_${System.currentTimeMillis()}"
            val speechDeferred = CompletableDeferred<Unit>()

            withContext(Dispatchers.Main) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        if (id == utteranceId && !speechDeferred.isCompleted) speechDeferred.complete(Unit)
                    }
                    override fun onError(id: String?) {
                        if (id == utteranceId && !speechDeferred.isCompleted) speechDeferred.complete(Unit)
                    }
                })

                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }

            // Damos tiempo a reproducir el audio antes de que el receiver o el executor finalicen
            withTimeoutOrNull(5000L) {
                speechDeferred.await()
            }

            "Pronunciando: \"$text\""
        } else {
            // Fallback sonoro del sistema si el motor TTS tarda o no está presente
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

