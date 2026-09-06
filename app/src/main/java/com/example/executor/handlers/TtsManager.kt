package com.example.executor.handlers

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.executor.ExecutionResult
import com.example.executor.VariableResolver
import java.util.Locale

class TtsManager(private val context: Context) {

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

    fun speak(text: String): ExecutionResult {
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

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
