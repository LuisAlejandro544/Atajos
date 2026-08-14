package com.example.engine.handlers

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

class FlashlightActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.TOGGLE_FLASHLIGHT)

    private var isTorchOn = false

    override suspend fun execute(action: ShortcutAction): String {
        val mode = action.param1.lowercase()
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return "Cámara no disponible"

        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull() ?: return "Sin flash"

            val targetState = when (mode) {
                "on" -> true
                "off" -> false
                else -> !isTorchOn
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, targetState)
                isTorchOn = targetState
                if (targetState) "Linterna encendida" else "Linterna apagada"
            } else {
                "Linterna no soportada en esta versión de Android"
            }
        } catch (e: CameraAccessException) {
            "Error al acceder a linterna: ${e.message}"
        } catch (e: Exception) {
            "Linterna: ${e.message}"
        }
    }
}
