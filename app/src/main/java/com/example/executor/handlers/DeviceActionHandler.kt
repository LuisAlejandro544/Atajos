package com.example.executor.handlers

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.example.executor.ExecutionResult
import kotlin.math.roundToInt

class DeviceActionHandler(private val context: Context) {

    companion object {
        private var isFlashlightOn = false
    }

    fun toggleFlashlight(): ExecutionResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return ExecutionResult(false, "Cámara no disponible")

        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }

            if (cameraId == null) {
                return ExecutionResult(false, "No se encontró flash")
            }

            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)
            val state = if (isFlashlightOn) "encendida" else "apagada"
            ExecutionResult(true, "Linterna $state")
        } catch (e: CameraAccessException) {
            ExecutionResult(false, "Flash inaccesible: ${e.message}")
        } catch (e: Exception) {
            ExecutionResult(false, "Error con linterna: ${e.message}")
        }
    }

    fun setBrightness(parameter: String): ExecutionResult {
        val clean = parameter.trim().removeSuffix("%")
        val percent = clean.toIntOrNull()?.coerceIn(0, 100) ?: 80

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
                return ExecutionResult(
                    false,
                    "Permiso requerido: concede permiso en la pantalla de ajustes de brillo."
                )
            }
        }

        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            val brightness255 = ((percent / 100f) * 255).roundToInt().coerceIn(1, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness255
            )
            ExecutionResult(true, "Brillo ajustado al $percent%")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al ajustar brillo: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {
            // Ignorar excepciones en dispositivos sin motor háptico
        }
    }
}
