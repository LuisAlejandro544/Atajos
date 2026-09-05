package com.example.data.model

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class ActionBlockConverter {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, ActionBlock::class.java)
    private val adapter = moshi.adapter<List<ActionBlock>>(listType)

    @TypeConverter
    fun fromActionBlockList(blocks: List<ActionBlock>?): String {
        if (blocks.isNullOrEmpty()) return "[]"
        return try {
            adapter.toJson(blocks)
        } catch (_: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toActionBlockList(json: String?): List<ActionBlock> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
