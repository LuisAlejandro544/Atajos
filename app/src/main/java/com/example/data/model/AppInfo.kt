package com.example.data.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Modelo de datos que representa una aplicación o juego instalado en el dispositivo.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val isGame: Boolean,
    val iconBitmap: ImageBitmap? = null
)
