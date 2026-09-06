package com.example.debug.telemetry

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

class TelemetryManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val telemetryFile = File(context.filesDir, "telemetry_history.json")

    private val _history = MutableStateFlow<List<ExecutionTelemetry>>(emptyList())
    val history: StateFlow<List<ExecutionTelemetry>> = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        scope.launch {
            try {
                if (telemetryFile.exists()) {
                    val raw = telemetryFile.readText()
                    val array = JSONArray(raw)
                    val list = mutableListOf<ExecutionTelemetry>()
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i)
                        if (obj != null) {
                            list.add(ExecutionTelemetry.fromJson(obj))
                        }
                    }
                    _history.value = list.sortedByDescending { it.timestamp }
                }
            } catch (_: Exception) {
                _history.value = emptyList()
            }
        }
    }

    fun recordExecution(telemetry: ExecutionTelemetry) {
        scope.launch {
            val current = _history.value.toMutableList()
            current.add(0, telemetry)
            // Conservar máximo 150 registros para optimización de memoria
            val trimmed = if (current.size > 150) current.take(150) else current
            _history.value = trimmed
            persistHistory(trimmed)
        }
    }

    fun clearTelemetry() {
        scope.launch {
            _history.value = emptyList()
            if (telemetryFile.exists()) {
                telemetryFile.delete()
            }
        }
    }

    private fun persistHistory(list: List<ExecutionTelemetry>) {
        try {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            telemetryFile.writeText(array.toString())
        } catch (_: Exception) {}
    }

    fun computeStats(): TelemetryStats {
        val list = _history.value
        if (list.isEmpty()) return TelemetryStats()

        val total = list.size
        val successCount = list.count { it.status == "EXITOSO" }
        val successRate = if (total > 0) (successCount * 100) / total else 0
        val avgDuration = if (total > 0) list.sumOf { it.totalDurationMs } / total else 0L
        val totalSteps = list.sumOf { it.steps.size }

        return TelemetryStats(
            totalExecutions = total,
            successfulExecutions = successCount,
            successRatePercent = successRate,
            averageDurationMs = avgDuration,
            totalStepsExecuted = totalSteps
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: TelemetryManager? = null

        fun getInstance(context: Context): TelemetryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TelemetryManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
