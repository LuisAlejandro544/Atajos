package com.example.piper

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.executor.ExecutionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.text.Normalizer

object PiperTtsManager {
    private const val TAG = "PiperTtsManager"

    val AVAILABLE_VOICES = listOf(
        PiperVoice(
            id = "es_ES-carlfm-x_low",
            name = "Español Carlfm (Neural Ligero)",
            description = "Voz neuronal rápida y liviana (16 kHz). Empaquetada e integrada offline.",
            sampleRate = 16000,
            modelFileName = "es_ES-carlfm-x_low.onnx",
            configFileName = "es_ES-carlfm-x_low.onnx.json",
            isBundled = true,
            downloadUrlModel = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/carlfm/x_low/es_ES-carlfm-x_low.onnx",
            downloadUrlConfig = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/carlfm/x_low/es_ES-carlfm-x_low.onnx.json"
        ),
        PiperVoice(
            id = "es_ES-davefx-medium",
            name = "Español Davefx (Neural Alta Fidelidad)",
            description = "Voz masculina natural de estudio (22.05 kHz, VITS Medium).",
            sampleRate = 22050,
            modelFileName = "es_ES-davefx-medium.onnx",
            configFileName = "es_ES-davefx-medium.onnx.json",
            isBundled = false,
            downloadUrlModel = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/davefx/medium/es_ES-davefx-medium.onnx",
            downloadUrlConfig = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/davefx/medium/es_ES-davefx-medium.onnx.json"
        ),
        PiperVoice(
            id = "es_ES-sharvard-medium",
            name = "Español Sharvard (Neural Femenina)",
            description = "Voz femenina expresiva y clara (22.05 kHz, VITS Medium).",
            sampleRate = 22050,
            modelFileName = "es_ES-sharvard-medium.onnx",
            configFileName = "es_ES-sharvard-medium.onnx.json",
            isBundled = false,
            downloadUrlModel = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/sharvard/medium/es_ES-sharvard-medium.onnx",
            downloadUrlConfig = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/sharvard/medium/es_ES-sharvard-medium.onnx.json"
        )
    )

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadStatusMessage = MutableStateFlow("")
    val downloadStatusMessage: StateFlow<String> = _downloadStatusMessage.asStateFlow()

    @Volatile
    private var ortEnv: OrtEnvironment? = null
    @Volatile
    private var activeSession: OrtSession? = null
    @Volatile
    private var activeVoiceId: String? = null
    @Volatile
    private var activeSampleRate: Int = 16000
    @Volatile
    private var activePhonemeIdMap: Map<String, List<Long>> = emptyMap()
    @Volatile
    private var noiseScale: Float = 0.667f
    @Volatile
    private var lengthScale: Float = 1.0f
    @Volatile
    private var noiseW: Float = 0.8f

    @Volatile
    private var activeAudioTrack: AudioTrack? = null
    private val synthLock = Any()

    fun getVoice(voiceId: String): PiperVoice {
        return AVAILABLE_VOICES.firstOrNull { it.id == voiceId } ?: AVAILABLE_VOICES[0]
    }

    fun isVoiceAvailable(context: Context, voiceId: String): Boolean {
        val voice = getVoice(voiceId)
        val modelsDir = File(context.filesDir, "piper_models")
        val modelFile = File(modelsDir, voice.modelFileName)
        val configFile = File(modelsDir, voice.configFileName)
        if (modelFile.exists() && modelFile.length() > 0 && configFile.exists() && configFile.length() > 0) {
            return true
        }
        // Si está empaquetado en assets
        if (voice.isBundled) {
            try {
                val list = context.assets.list("piper") ?: emptyArray()
                return list.contains(voice.modelFileName) && list.contains(voice.configFileName)
            } catch (_: Exception) {
                return false
            }
        }
        return false
    }

    fun ensureVoiceReady(context: Context, voiceId: String): Boolean {
        val voice = getVoice(voiceId)
        val modelsDir = File(context.filesDir, "piper_models")
        if (!modelsDir.exists()) modelsDir.mkdirs()

        val targetModel = File(modelsDir, voice.modelFileName)
        val targetConfig = File(modelsDir, voice.configFileName)

        if (targetModel.exists() && targetModel.length() > 0 && targetConfig.exists() && targetConfig.length() > 0) {
            return true
        }

        if (voice.isBundled) {
            try {
                context.assets.open("piper/${voice.modelFileName}").use { input ->
                    FileOutputStream(targetModel).use { output ->
                        input.copyTo(output)
                    }
                }
                context.assets.open("piper/${voice.configFileName}").use { input ->
                    FileOutputStream(targetConfig).use { output ->
                        input.copyTo(output)
                    }
                }
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error extrayendo modelo empaquetado de Piper: ${e.message}")
                return false
            }
        }
        return false
    }

    fun loadVoiceSession(context: Context, voiceId: String): Boolean {
        synchronized(synthLock) {
            if (activeSession != null && activeVoiceId == voiceId) {
                return true
            }

            if (!ensureVoiceReady(context, voiceId)) {
                return false
            }

            val voice = getVoice(voiceId)
            val modelsDir = File(context.filesDir, "piper_models")
            val targetModel = File(modelsDir, voice.modelFileName)
            val targetConfig = File(modelsDir, voice.configFileName)

            try {
                // Leer configuración JSON
                val jsonString = targetConfig.readText()
                val rootJson = JSONObject(jsonString)

                val audioObj = rootJson.optJSONObject("audio")
                activeSampleRate = audioObj?.optInt("sample_rate", voice.sampleRate) ?: voice.sampleRate

                val infObj = rootJson.optJSONObject("inference")
                noiseScale = infObj?.optDouble("noise_scale", 0.667)?.toFloat() ?: 0.667f
                lengthScale = infObj?.optDouble("length_scale", 1.0)?.toFloat() ?: 1.0f
                noiseW = infObj?.optDouble("noise_w", 0.8)?.toFloat() ?: 0.8f

                val phonemeMap = mutableMapOf<String, List<Long>>()
                val idMapJson = rootJson.optJSONObject("phoneme_id_map")
                if (idMapJson != null) {
                    val keys = idMapJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val arr = idMapJson.getJSONArray(key)
                        val list = mutableListOf<Long>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getLong(i))
                        }
                        phonemeMap[key] = list
                    }
                }
                activePhonemeIdMap = phonemeMap

                // Crear sesión ONNX
                val env = ortEnv ?: OrtEnvironment.getEnvironment().also { ortEnv = it }
                val opts = OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(2)
                }

                activeSession?.close()
                activeSession = env.createSession(targetModel.absolutePath, opts)
                activeVoiceId = voiceId
                Log.i(TAG, "Sesión de Piper TTS cargada: $voiceId (sampleRate: $activeSampleRate Hz)")
                return true
            } catch (e: Throwable) {
                Log.e(TAG, "Fallo al inicializar sesión ONNX de Piper TTS: ${e.message}")
                return false
            }
        }
    }

    /**
     * Convierte texto en español a secuencia de fonemas / IDs según phoneme_id_map de Piper.
     */
    fun textToPhonemeIds(text: String, speedMultiplier: Float = 1.0f): LongArray {
        val normalized = text.lowercase().trim()
        val sequence = mutableListOf<Long>()

        val padId = activePhonemeIdMap["_"] ?: listOf(0L)
        val startId = activePhonemeIdMap["^"] ?: listOf(1L)
        val endId = activePhonemeIdMap["$"] ?: listOf(2L)

        sequence.addAll(startId)
        sequence.addAll(padId)

        var i = 0
        while (i < normalized.length) {
            val c = normalized[i]

            // Manejo de dígrafos en español
            var token = c.toString()
            if (i + 1 < normalized.length) {
                val pair = "${normalized[i]}${normalized[i+1]}"
                when (pair) {
                    "ll" -> { token = "ʎ"; i++ }
                    "ch" -> { token = "tʃ"; i++ }
                    "rr" -> { token = "r"; i++ }
                    "qu" -> { token = "k"; i++ }
                }
            }

            // Normalización de caracteres y fonética española
            val mappedToken = when (token) {
                "á" -> "a"
                "é" -> "e"
                "í" -> "i"
                "ó" -> "o"
                "ú", "ü" -> "u"
                "ñ" -> "ɲ"
                "z" -> "θ"
                "v" -> "b"
                "¿" -> "?"
                "¡" -> "!"
                "j" -> "x"
                else -> token
            }

            // Buscar en phoneme_id_map
            val ids = activePhonemeIdMap[mappedToken] 
                ?: activePhonemeIdMap[token]
                ?: activePhonemeIdMap[c.toString()]

            if (ids != null) {
                sequence.addAll(ids)
                sequence.addAll(padId)
            } else if (c == ' ') {
                val spaceIds = activePhonemeIdMap[" "] ?: listOf(3L)
                sequence.addAll(spaceIds)
                sequence.addAll(padId)
            }

            i++
        }

        sequence.addAll(endId)
        return sequence.toLongArray()
    }

    /**
     * Sintetiza y reproduce voz con Piper TTS.
     */
    fun speak(
        context: Context,
        text: String,
        voiceId: String = "es_ES-carlfm-x_low",
        speedMultiplier: Float = 1.0f
    ): ExecutionResult {
        if (text.isBlank()) return ExecutionResult(false, "El texto para Piper TTS está vacío")

        synchronized(synthLock) {
            try {
                if (!loadVoiceSession(context, voiceId)) {
                    return ExecutionResult(false, "No se pudo cargar la voz de Piper TTS: $voiceId")
                }

                val session = activeSession ?: return ExecutionResult(false, "Sesión de Piper TTS no disponible")
                val env = ortEnv ?: OrtEnvironment.getEnvironment().also { ortEnv = it }

                val phonemeIds = textToPhonemeIds(text, speedMultiplier)
                if (phonemeIds.isEmpty()) {
                    return ExecutionResult(false, "No se generaron fonemas válidos para el texto")
                }

                val inputTensor = OnnxTensor.createTensor(
                    env,
                    LongBuffer.wrap(phonemeIds),
                    longArrayOf(1, phonemeIds.size.toLong())
                )
                val lengthsTensor = OnnxTensor.createTensor(
                    env,
                    LongBuffer.wrap(longArrayOf(phonemeIds.size.toLong())),
                    longArrayOf(1)
                )

                // Ajustar velocidad con length_scale (mayor = más lento, menor = más rápido)
                val effectiveLengthScale = if (speedMultiplier > 0f) lengthScale / speedMultiplier else lengthScale
                val scalesTensor = OnnxTensor.createTensor(
                    env,
                    FloatBuffer.wrap(floatArrayOf(noiseScale, effectiveLengthScale, noiseW)),
                    longArrayOf(3)
                )

                val inputs = mutableMapOf<String, OnnxTensor>(
                    "input" to inputTensor,
                    "input_lengths" to lengthsTensor,
                    "scales" to scalesTensor
                )

                if (session.inputNames.contains("sid")) {
                    inputs["sid"] = OnnxTensor.createTensor(
                        env,
                        LongBuffer.wrap(longArrayOf(0L)),
                        longArrayOf(1)
                    )
                }

                val startTime = System.currentTimeMillis()
                val results = session.run(inputs)
                val inferenceTimeMs = System.currentTimeMillis() - startTime

                val outputTensor = results.get(0)
                val rawAudioFloats = extractFloatArray(outputTensor.value)

                // Limpiar tensores de entrada
                inputTensor.close()
                lengthsTensor.close()
                scalesTensor.close()
                inputs["sid"]?.close()
                results.close()

                if (rawAudioFloats.isEmpty()) {
                    return ExecutionResult(false, "Piper TTS no produjo muestras de audio")
                }

                // Reproducir el audio resultante vía AudioTrack
                playAudio(rawAudioFloats, activeSampleRate)

                return ExecutionResult(
                    true,
                    "Piper TTS: voz sintetizada en ${inferenceTimeMs}ms (${rawAudioFloats.size} muestras a ${activeSampleRate}Hz)"
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Error durante síntesis Piper TTS: ${e.message}", e)
                return ExecutionResult(false, "Error en Piper TTS: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    private fun extractFloatArray(value: Any?): FloatArray {
        return when (value) {
            is FloatArray -> value
            is Array<*> -> {
                val list = mutableListOf<Float>()
                flattenFloats(value, list)
                list.toFloatArray()
            }
            else -> floatArrayOf()
        }
    }

    private fun flattenFloats(arr: Array<*>, target: MutableList<Float>) {
        for (item in arr) {
            when (item) {
                is FloatArray -> for (f in item) target.add(f)
                is Array<*> -> flattenFloats(item, target)
                is Number -> target.add(item.toFloat())
            }
        }
    }

    private fun playAudio(audioFloats: FloatArray, sampleRate: Int) {
        try {
            stopAudio()

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
            )
            val bufferSize = maxOf(minBufferSize, audioFloats.size * 4)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            activeAudioTrack = track
            track.play()
            track.write(audioFloats, 0, audioFloats.size, AudioTrack.WRITE_BLOCKING)
            track.stop()
            track.release()
            if (activeAudioTrack === track) {
                activeAudioTrack = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo buffer PCM con AudioTrack: ${e.message}")
        }
    }

    fun stopAudio() {
        try {
            activeAudioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                    track.flush()
                    track.stop()
                }
                track.release()
            }
        } catch (_: Exception) {}
        activeAudioTrack = null
    }

    /**
     * Descarga de modelos adicionales desde HuggingFace con telemetría OkHttp/Chucker.
     */
    fun downloadVoiceAsync(
        context: Context,
        voiceId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val voice = getVoice(voiceId)
        val modelsDir = File(context.filesDir, "piper_models")
        if (!modelsDir.exists()) modelsDir.mkdirs()

        _isDownloading.value = true
        _downloadProgress.value = 0.05f
        _downloadStatusMessage.value = "Iniciando descarga de ${voice.name}..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                // 1. Descargar Configuración JSON
                _downloadStatusMessage.value = "Descargando configuración de fonemas..."
                val configReq = Request.Builder().url(voice.downloadUrlConfig).build()
                client.newCall(configReq).execute().use { resp ->
                    if (!resp.isSuccessful) throw Exception("Error HTTP ${resp.code} al descargar config")
                    val body = resp.body ?: throw Exception("Cuerpo de respuesta vacío")
                    val targetConfig = File(modelsDir, voice.configFileName)
                    FileOutputStream(targetConfig).use { out ->
                        body.byteStream().copyTo(out)
                    }
                }
                _downloadProgress.value = 0.2f

                // 2. Descargar Modelo ONNX con progreso
                _downloadStatusMessage.value = "Descargando red neuronal ONNX..."
                val modelReq = Request.Builder().url(voice.downloadUrlModel).build()
                client.newCall(modelReq).execute().use { resp ->
                    if (!resp.isSuccessful) throw Exception("Error HTTP ${resp.code} al descargar modelo")
                    val body = resp.body ?: throw Exception("Cuerpo de modelo vacío")
                    val totalBytes = body.contentLength()
                    val targetModel = File(modelsDir, voice.modelFileName)

                    var bytesRead = 0L
                    val buffer = ByteArray(8192)
                    body.byteStream().use { input ->
                        FileOutputStream(targetModel).use { output ->
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                bytesRead += read
                                if (totalBytes > 0) {
                                    val progress = 0.2f + (bytesRead.toFloat() / totalBytes) * 0.8f
                                    _downloadProgress.value = progress
                                    val mbRead = bytesRead / (1024 * 1024)
                                    val mbTotal = totalBytes / (1024 * 1024)
                                    _downloadStatusMessage.value = "Descargando ONNX: ${mbRead}MB de ${mbTotal}MB (${(progress * 100).toInt()}%)"
                                }
                            }
                        }
                    }
                }

                _downloadProgress.value = 1.0f
                _downloadStatusMessage.value = "¡Descarga de ${voice.name} completada!"
                _isDownloading.value = false

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en descarga de Piper: ${e.message}", e)
                _isDownloading.value = false
                _downloadStatusMessage.value = "Error: ${e.localizedMessage ?: "Fallo de descarga"}"
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Error de red")
                }
            }
        }
    }
}
