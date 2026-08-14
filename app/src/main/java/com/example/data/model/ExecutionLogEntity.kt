package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val shortcutId: Long,
    val shortcutTitle: String,
    val iconKey: String,
    val colorHex: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // SUCCESS, FAILED
    val actionCount: Int = 1,
    val durationMs: Long = 0L,
    val summary: String = "Ejecutado con éxito"
)
