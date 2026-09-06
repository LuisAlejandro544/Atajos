package com.example.executor.handlers

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.espeak.EspeakManager
import com.example.executor.ExecutionResult
import com.example.executor.VariableResolver
import com.example.piper.PiperTtsManager
import com.example.tts.TtsEngineType
import com.example.tts.TtsPreferences
import org.json.JSONObject
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

        // Inicialización asíncrona de eSpeak-NG
        EspeakManager.initAsync(context)
    }

    fun speak(
        textOrJson: String,
        engineOverride: TtsEngineType? = null,
        voiceOverride: String? = null
    ): ExecutionResult {
        var rawText = textOrJson.trim()
        var targetEngine = engineOverride ?: TtsPreferences.getSelectedEngine(context)
        var targetVoice = voiceOverride

        // Permitir parámetro estructurado en formato JSON
        if (rawText.startsWith("{") && rawText.endsWith("}")) {
            try {
                val json = JSONObject(rawText)
                if (json.has("text")) {
                    rawText = json.getString("text")
                }
                if (json.has("engine")) {
                    targetEngine = TtsEngineType.fromId(json.getString("engine"))
                }
                if (json.has("voice")) {
                    targetVoice = json.getString("voice")
                }
            } catch (_: Exception) {}
        }

        val messageToSpeak = VariableResolver.resolve(rawText.ifEmpty { "Atajo ejecutado" }, context)

        return when (targetEngine) {
            TtsEngineType.PIPER -> {
                val piperVoice = targetVoice ?: TtsPreferences.getSelectedPiperVoice(context)
                val speed = TtsPreferences.getSpeechSpeed(context)
                val piperResult = PiperTtsManager.speak(context, messageToSpeak, piperVoice, speed)
                if (piperResult.success) {
                    piperResult
                } else {
                    // Fallback transparente al motor del sistema si Piper encuentra algún fallo
                    val fallback = speakWithSystemTts(messageToSpeak)
                    ExecutionResult(
                        fallback.success,
                        "${piperResult.message}. (Fallback) ${fallback.message}"
                    )
                }
            }
            TtsEngineType.ESPEAK -> {
                val espeakVoice = targetVoice ?: TtsPreferences.getSelectedEspeakVoice(context)
                val success = EspeakManager.speak(context, messageToSpeak, espeakVoice)
                if (success) {
                    ExecutionResult(true, "eSpeak-NG: reproduciendo voz ($espeakVoice)")
                } else {
                    speakWithSystemTts(messageToSpeak)
                }
            }
            TtsEngineType.SYSTEM -> {
                speakWithSystemTts(messageToSpeak)
            }
        }
    }

    private fun speakWithSystemTts(messageToSpeak: String): ExecutionResult {
        return try {
            if (tts == null) {
                ExecutionResult(false, "Servicio de Texto a Voz del sistema no disponible")
            } else if (!isTtsInitialized) {
                synchronized(pendingSpeechQueue) {
                    pendingSpeechQueue.add(messageToSpeak)
                }
                ExecutionResult(true, "Voz en inicialización: texto puesto en cola")
            } else {
                val pitch = TtsPreferences.getSpeechPitch(context)
                val speed = TtsPreferences.getSpeechSpeed(context)
                tts?.setPitch(pitch)
                tts?.setSpeechRate(speed)

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
            PiperTtsManager.stopAudio()
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
