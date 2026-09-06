package com.example.espeak

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.reecedunn.espeak.SpeechSynthesis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

data class EspeakVoice(
    val id: String,
    val name: String,
    val description: String
)

object EspeakManager {
    private const val TAG = "EspeakManager"

    val SPANISH_VOICES = listOf(
        EspeakVoice("es", "Español (España)", "Voz estándar de España"),
        EspeakVoice("es-419", "Español (Latinoamérica)", "Voz estándar latinoamericana"),
        EspeakVoice("es+m1", "Español Masculino", "Variante masculina con timbre grave"),
        EspeakVoice("es+f1", "Español Femenino", "Variante femenina clara"),
        EspeakVoice("es+croak", "Español Robótico", "Variante sintética estilo androide"),
        EspeakVoice("es+whisper", "Español Susurro", "Variante sutil y atenuada")
    )

    @Volatile
    private var synthesis: SpeechSynthesis? = null
    @Volatile
    private var isReady = false
    @Volatile
    private var isInitializing = false

    private var activeAudioTrack: AudioTrack? = null
    private val audioLock = Any()

    fun initAsync(context: Context, onReady: (() -> Unit)? = null) {
        if (isReady) {
            onReady?.invoke()
            return
        }
        if (isInitializing) return
        isInitializing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ensureVoiceDataExtracted(context)
                val dataDir = context.filesDir.absolutePath
                val synth = SpeechSynthesis(dataDir, object : SpeechSynthesis.SynthCallback {
                    override fun onSynthDataReady(audioData: ByteArray?) {
                        if (audioData != null && audioData.isNotEmpty()) {
                            synchronized(audioLock) {
                                activeAudioTrack?.write(audioData, 0, audioData.size)
                            }
                        }
                    }

                    override fun onSynthDataComplete() {
                        // Síntesis finalizada
                    }
                })

                if (synth.isInitialized) {
                    synthesis = synth
                    isReady = true
                    Log.i(TAG, "eSpeak-NG motor listo para síntesis de voz en español")
                } else {
                    Log.e(TAG, "No se pudo inicializar SpeechSynthesis de eSpeak-NG")
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error inicializando eSpeak-NG: ${e.message}")
            } finally {
                isInitializing = false
                if (isReady) {
                    withContext(Dispatchers.Main) {
                        onReady?.invoke()
                    }
                }
            }
        }
    }

    private fun ensureVoiceDataExtracted(context: Context) {
        val targetDir = File(context.filesDir, "espeak-ng-data")
        val versionFile = File(targetDir, "version")
        val esDictFile = File(targetDir, "es_dict")

        if (targetDir.exists() && versionFile.exists() && esDictFile.exists()) {
            return
        }

        try {
            targetDir.mkdirs()
            context.assets.open("espeakdata.zip").use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        val outFile = File(context.filesDir, entry.name)
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { out ->
                                zipStream.copyTo(out)
                            }
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }
            Log.i(TAG, "Datos de eSpeak-NG extraídos exitosamente en ${targetDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error extrayendo datos de eSpeak-NG: ${e.message}")
        }
    }

    fun speak(context: Context, text: String, voiceId: String = "es"): Boolean {
        if (!isReady || synthesis == null) {
            initAsync(context) {
                speak(context, text, voiceId)
            }
            return true
        }

        val synth = synthesis ?: return false
        stop()

        return try {
            val sampleRate = synth.sampleRate
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            synchronized(audioLock) {
                activeAudioTrack = audioTrack
            }

            audioTrack.play()
            synth.setVoiceByName(voiceId)
            synth.synthesize(text)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error al sintetizar con eSpeak-NG: ${e.message}")
            false
        }
    }

    fun stop() {
        try {
            synthesis?.stop()
            synchronized(audioLock) {
                activeAudioTrack?.stop()
                activeAudioTrack?.release()
                activeAudioTrack = null
            }
        } catch (_: Throwable) {}
    }
}
