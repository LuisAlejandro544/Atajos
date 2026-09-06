package com.example.tts

import android.content.Context
import android.content.SharedPreferences

object TtsPreferences {
    private const val PREFS_NAME = "shortcuts_tts_prefs"
    private const val KEY_SELECTED_ENGINE = "selected_engine"
    private const val KEY_PIPER_VOICE = "selected_piper_voice"
    private const val KEY_ESPEAK_VOICE = "selected_espeak_voice"
    private const val KEY_SPEECH_SPEED = "speech_speed"
    private const val KEY_SPEECH_PITCH = "speech_pitch"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedEngine(context: Context): TtsEngineType {
        val id = getPrefs(context).getString(KEY_SELECTED_ENGINE, TtsEngineType.PIPER.id) ?: TtsEngineType.PIPER.id
        return TtsEngineType.fromId(id)
    }

    fun setSelectedEngine(context: Context, engine: TtsEngineType) {
        getPrefs(context).edit().putString(KEY_SELECTED_ENGINE, engine.id).apply()
    }

    fun getSelectedPiperVoice(context: Context): String {
        return getPrefs(context).getString(KEY_PIPER_VOICE, "es_ES-carlfm-x_low") ?: "es_ES-carlfm-x_low"
    }

    fun setSelectedPiperVoice(context: Context, voiceId: String) {
        getPrefs(context).edit().putString(KEY_PIPER_VOICE, voiceId).apply()
    }

    fun getSelectedEspeakVoice(context: Context): String {
        return getPrefs(context).getString(KEY_ESPEAK_VOICE, "es") ?: "es"
    }

    fun setSelectedEspeakVoice(context: Context, voiceId: String) {
        getPrefs(context).edit().putString(KEY_ESPEAK_VOICE, voiceId).apply()
    }

    fun getSpeechSpeed(context: Context): Float {
        return getPrefs(context).getFloat(KEY_SPEECH_SPEED, 1.0f)
    }

    fun setSpeechSpeed(context: Context, speed: Float) {
        getPrefs(context).edit().putFloat(KEY_SPEECH_SPEED, speed).apply()
    }

    fun getSpeechPitch(context: Context): Float {
        return getPrefs(context).getFloat(KEY_SPEECH_PITCH, 1.0f)
    }

    fun setSpeechPitch(context: Context, pitch: Float) {
        getPrefs(context).edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
    }
}
