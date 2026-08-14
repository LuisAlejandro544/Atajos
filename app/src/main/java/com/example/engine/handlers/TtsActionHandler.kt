package com.example.engine.handlers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class TtsActionHandler(private val context: Context) : ActionHandler, TextToSpeech.OnInitListener {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.SPEAK_TEXT)

    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady = false
    private val initDeferred = CompletableDeferred<Boolean>()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error instanciando TextToSpeech", e)
            initDeferred.complete(false)
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
                initDeferred.complete(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error configurando idioma TTS", e)
                initDeferred.complete(false)
            }
        } else {
            Log.e(TAG, "Fallo al inicializar TTS con status: $status")
            initDeferred.complete(false)
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

        // Si el motor TTS aún se está inicializando (típico en disparadores en segundo plano),
        // esperamos hasta 2000ms a que termine de conectarse con el servicio de voz.
        if (!isTtsReady) {
            withTimeoutOrNull(2000L) {
                initDeferred.await()
            }
        }

        return if (tts != null && isTtsReady) {
            val utteranceId = "atajos_tts_${System.currentTimeMillis()}"
            val speechDeferred = CompletableDeferred<Unit>()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) speechDeferred.complete(Unit)
                }
                override fun onError(id: String?) {
                    if (id == utteranceId) speechDeferred.complete(Unit)
                }
            })

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            
            // Damos tiempo a reproducir el audio antes de que el receiver o el executor finalicen
            withTimeoutOrNull(4000L) {
                speechDeferred.await()
            }

            "Pronunciando: \"$text\""
        } else {
            // Fallback en hilo principal para el Toast
            Handler(Looper.getMainLooper()).post {
                try {
                    Toast.makeText(context.applicationContext, "Voz: $text", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error mostrando Toast de fallback", e)
                }
            }
            "Voz (alerta): \"$text\""
        }
    }

    override fun onCancelled() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "TtsActionHandler"
    }
}

