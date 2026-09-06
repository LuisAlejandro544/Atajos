package com.example.executor.handlers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.example.executor.ExecutionResult

class CommunicationActionHandler(private val context: Context) {

    fun copyToClipboard(text: String): ExecutionResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return ExecutionResult(false, "Portapapeles no disponible")

        val clip = ClipData.newPlainText("Atajo", text)
        clipboard.setPrimaryClip(clip)
        val preview = if (text.length > 28) text.take(25) + "..." else text
        return ExecutionResult(true, "Copiado: \"$preview\"")
    }

    fun sendMessage(messageText: String): ExecutionResult {
        val text = messageText.trim().ifEmpty { "Hola" }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Enviar mensaje rápido").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(chooser)
            ExecutionResult(true, "Preparando envío de mensaje")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al enviar mensaje: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    fun shareText(text: String): ExecutionResult {
        val content = text.trim().ifEmpty { "Texto compartido" }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Compartir con").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(chooser)
            ExecutionResult(true, "Compartiendo texto")
        } catch (e: Exception) {
            ExecutionResult(false, "Error al compartir texto: ${e.localizedMessage ?: "Fallo"}")
        }
    }

    fun setTimer(minutesParam: String): ExecutionResult {
        val minutes = minutesParam.trim().toIntOrNull() ?: 5
        val seconds = (minutes * 60).coerceAtLeast(1)

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Atajo ($minutes min)")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Temporizador de $minutes min iniciado")
        } catch (_: Exception) {
            ExecutionResult(false, "App de reloj no disponible")
        }
    }
}
