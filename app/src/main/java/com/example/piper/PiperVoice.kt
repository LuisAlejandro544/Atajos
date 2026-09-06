package com.example.piper

data class PiperVoice(
    val id: String,
    val name: String,
    val description: String,
    val sampleRate: Int = 16000,
    val modelFileName: String,
    val configFileName: String,
    val isBundled: Boolean = false,
    val downloadUrlModel: String,
    val downloadUrlConfig: String
)
