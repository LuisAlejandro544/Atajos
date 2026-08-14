package com.example.engine.handlers

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import java.util.Locale

class TtsActionHandler(private val context: Context) : ActionHandler, TextToSpeech.OnInitListener {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.SPEAK_TEXT)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            isTtsReady = true
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
        return if (tts != null && isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "atajos_tts_${System.currentTimeMillis()}")
            "Pronunciando: \"$text\""
        } else {
            Toast.makeText(context, "Voz: $text", Toast.LENGTH_SHORT).show()
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
}
