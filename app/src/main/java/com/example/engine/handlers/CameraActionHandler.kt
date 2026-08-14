package com.example.engine.handlers

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

/**
 * Manejador especializado para la apertura directa de la cámara del dispositivo
 * en modo Foto normal, Selfie (cámara frontal) o Grabación de Vídeo.
 */
class CameraActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.OPEN_CAMERA)

    override suspend fun execute(action: ShortcutAction): String {
        val mode = action.param1.trim().lowercase().ifBlank { "photo" }

        return try {
            val intent = when (mode) {
                "video", "video_capture", "grabar" -> {
                    Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                "selfie", "front", "frontal" -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        // Parámetros estándar de fabricantes para abrir cámara frontal
                        putExtra("android.intent.extras.CAMERA_FACING", 1)
                        putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                        putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                    }
                }
                else -> {
                    // Modo Foto normal (cámara trasera)
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("android.intent.extras.CAMERA_FACING", 0)
                        putExtra("android.intent.extras.LENS_FACING_BACK", 1)
                    }
                }
            }

            // Si el intent no tiene actividad receptora directa para ACTION_IMAGE_CAPTURE/VIDEO,
            // fallback al intent general de captura o visor de cámara
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                val genericIntent = when (mode) {
                    "video", "video_capture", "grabar" -> Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
                    else -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                }.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                if (genericIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(genericIntent)
                } else {
                    val fallbackIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                }
            }

            when (mode) {
                "video", "video_capture", "grabar" -> "Cámara abierta en modo Grabación de Vídeo"
                "selfie", "front", "frontal" -> "Cámara abierta en modo Selfie"
                else -> "Cámara abierta en modo Foto"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al abrir la cámara: ${e.localizedMessage ?: "Cámara no disponible"}"
        }
    }
}
