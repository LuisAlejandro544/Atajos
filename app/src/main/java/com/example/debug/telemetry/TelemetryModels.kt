package com.example.debug.telemetry

import org.json.JSONArray
import org.json.JSONObject

data class StepTelemetry(
    val stepIndex: Int,
    val actionType: String,
    val stepLabel: String,
    val durationMs: Long,
    val resultMessage: String,
    val success: Boolean
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("stepIndex", stepIndex)
        put("actionType", actionType)
        put("stepLabel", stepLabel)
        put("durationMs", durationMs)
        put("resultMessage", resultMessage)
        put("success", success)
    }

    companion object {
        fun fromJson(json: JSONObject): StepTelemetry = StepTelemetry(
            stepIndex = json.optInt("stepIndex", 1),
            actionType = json.optString("actionType", ""),
            stepLabel = json.optString("stepLabel", ""),
            durationMs = json.optLong("durationMs", 0L),
            resultMessage = json.optString("resultMessage", ""),
            success = json.optBoolean("success", true)
        )
    }
}

data class ExecutionTelemetry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val shortcutId: Long,
    val shortcutTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalDurationMs: Long,
    val status: String, // "EXITOSO", "ADVERTENCIA", "CANCELADO", "FALLIDO"
    val finalMessage: String,
    val steps: List<StepTelemetry> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("shortcutId", shortcutId)
        put("shortcutTitle", shortcutTitle)
        put("timestamp", timestamp)
        put("totalDurationMs", totalDurationMs)
        put("status", status)
        put("finalMessage", finalMessage)
        val array = JSONArray()
        steps.forEach { array.put(it.toJson()) }
        put("steps", array)
    }

    companion object {
        fun fromJson(json: JSONObject): ExecutionTelemetry {
            val stepsList = mutableListOf<StepTelemetry>()
            val array = json.optJSONArray("steps")
            if (array != null) {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i)
                    if (obj != null) {
                        stepsList.add(StepTelemetry.fromJson(obj))
                    }
                }
            }
            return ExecutionTelemetry(
                id = json.optString("id", java.util.UUID.randomUUID().toString()),
                shortcutId = json.optLong("shortcutId", 0L),
                shortcutTitle = json.optString("shortcutTitle", "Atajo"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                totalDurationMs = json.optLong("totalDurationMs", 0L),
                status = json.optString("status", "EXITOSO"),
                finalMessage = json.optString("finalMessage", ""),
                steps = stepsList
            )
        }
    }
}

data class TelemetryStats(
    val totalExecutions: Int = 0,
    val successfulExecutions: Int = 0,
    val successRatePercent: Int = 100,
    val averageDurationMs: Long = 0L,
    val totalStepsExecuted: Int = 0
)
