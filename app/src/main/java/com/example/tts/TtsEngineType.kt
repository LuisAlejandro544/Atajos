package com.example.tts

enum class TtsEngineType(
    val id: String,
    val title: String,
    val subtitle: String
) {
    PIPER("PIPER", "Piper TTS (Neuronal)", "Síntesis neuronal local VITS de alta fidelidad"),
    SYSTEM("SYSTEM", "Voz del Sistema", "Motor estándar predeterminado de Android"),
    ESPEAK("ESPEAK", "eSpeak-NG", "Sintetizador acústico ligero offline");

    companion object {
        fun fromId(id: String): TtsEngineType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PIPER
        }
    }
}
