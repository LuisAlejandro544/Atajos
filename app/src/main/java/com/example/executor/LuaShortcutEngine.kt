package com.example.executor

import android.content.Context
import android.util.Log

class LuaShortcutEngine(
    private val context: Context,
    private val executor: ShortcutExecutor
) {

    private val logs = mutableListOf<String>()

    companion object {
        private const val TAG = "LuaShortcutEngine"
        private var isNativeLoaded = false

        init {
            try {
                System.loadLibrary("native-lua")
                isNativeLoaded = true
                Log.i(TAG, "Motor nativo Lua 5.4.7 cargado correctamente")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Librería nativa no cargada (posible entorno de pruebas JVM): ${e.message}")
                isNativeLoaded = false
            }
        }
    }

    private external fun nativeExecuteScript(script: String): String
    private external fun nativeGetVersion(): String

    fun getLuaVersion(): String {
        return if (isNativeLoaded) {
            try {
                nativeGetVersion()
            } catch (e: Throwable) {
                "Lua 5.4.7 (Nativo)"
            }
        } else {
            "Lua 5.4.7"
        }
    }

    fun executeScript(luaCode: String): ExecutionResult {
        if (!isNativeLoaded) {
            return ExecutionResult(false, "Error: Motor nativo Lua 5.4.7 no inicializado en este entorno")
        }

        return try {
            logs.clear()
            val rawOutput = nativeExecuteScript(luaCode)

            val isError = rawOutput.startsWith("Error en Lua 5.4.7:") || rawOutput.startsWith("Error:")
            if (isError) {
                ExecutionResult(false, rawOutput)
            } else {
                val fullMsg = if (logs.isNotEmpty() && !rawOutput.contains(logs.last())) {
                    "${logs.joinToString(" | ")} | $rawOutput"
                } else {
                    rawOutput
                }
                ExecutionResult(true, "Lua: $fullMsg")
            }
        } catch (e: Throwable) {
            ExecutionResult(false, "Excepción Lua 5.4.7: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    // Callbacks invoked by C++ Lua 5.4.7 via JNI
    @Suppress("unused")
    fun onFlashlight(enabled: Boolean) {
        val result = executor.executeSingleBlock("FLASHLIGHT", "")
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onCopy(text: String) {
        val result = executor.executeSingleBlock("COPY_TEXT", text)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onOpenUrl(url: String) {
        val result = executor.executeSingleBlock("OPEN_URL", url)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onOpenApp(packageName: String) {
        val result = executor.executeSingleBlock("OPEN_APP", packageName)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onSetVolume(percent: Int) {
        val result = executor.executeSingleBlock("SET_VOLUME", percent.toString())
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onMap(query: String) {
        val result = executor.executeSingleBlock("MAP_NAV", query)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onTimer(param: String) {
        val result = executor.executeSingleBlock("SET_TIMER", param)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onMessage(phone: String, text: String) {
        val msg = if (phone.isNotBlank()) "$phone: $text" else text
        val result = executor.executeSingleBlock("SEND_MESSAGE", msg)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onSoundSettings() {
        val result = executor.executeSingleBlock("SOUND_SETTINGS", "")
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onShare(text: String) {
        val result = executor.executeSingleBlock("SHARE_TEXT", text)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onSpeak(text: String) {
        val result = executor.executeSingleBlock("SPEAK", text)
        logs.add(result.message)
    }

    @Suppress("unused")
    fun onGetHour(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }

    @Suppress("unused")
    fun onPrint(message: String) {
        logs.add(message)
    }
}
