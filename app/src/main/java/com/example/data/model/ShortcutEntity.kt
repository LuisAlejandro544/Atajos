package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val colorHex: String = "#4F46E5", // Modern indigo
    val iconKey: String = "flash_on",
    val category: String = "General",
    val actionsJson: String = "[]",
    val isFavorite: Boolean = false,
    val runCount: Int = 0,
    val lastRunTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
