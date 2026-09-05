package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ActionBlock(
    val id: String = UUID.randomUUID().toString(),
    val actionType: String,
    val parameter: String = "",
    val customLabel: String = ""
)
