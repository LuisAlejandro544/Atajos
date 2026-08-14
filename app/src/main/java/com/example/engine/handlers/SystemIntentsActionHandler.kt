package com.example.engine.handlers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import java.net.URLEncoder

class SystemIntentsActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(
        ActionType.OPEN_URL,
        ActionType.SEARCH_WEB,
        ActionType.COPY_CLIPBOARD,
        ActionType.SEND_WHATSAPP,
        ActionType.SEND_SMS,
        ActionType.SHARE_TEXT,
        ActionType.SET_TIMER
    )

    override suspend fun execute(action: ShortcutAction): String {
        return when (action.type) {
            ActionType.OPEN_URL -> {
                var url = action.param1.ifBlank { "https://google.com" }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Abriendo sitio web: $url"
            }

            ActionType.SEARCH_WEB -> {
                val query = action.param1.ifBlank { "Android Shortcuts" }
                val searchUrl = "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Buscando en Google: $query"
            }

            ActionType.COPY_CLIPBOARD -> {
                val textToCopy = action.param1.ifBlank { "Texto copiado desde Atajos" }
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("Atajos", textToCopy)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                "Texto copiado al portapapeles"
            }

            ActionType.SEND_WHATSAPP -> {
                val phone = action.param1.trim().replace("+", "").replace(" ", "").replace("-", "")
                val rawText = action.param2.ifBlank { "Hola, te escribo desde mis Atajos" }
                val text = com.example.engine.VariableResolverHelper.resolve(rawText, context)
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val uri = if (phone.isNotEmpty()) {
                    Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedText")
                } else {
                    Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
                }
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Abriendo WhatsApp"
            }

            ActionType.SEND_SMS -> {
                val phone = action.param1.trim()
                val rawText = action.param2.ifBlank { "Mensaje automático desde Atajos" }
                val text = com.example.engine.VariableResolverHelper.resolve(rawText, context)
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$phone")
                    putExtra("sms_body", text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Preparando SMS a $phone"
            }

            ActionType.SHARE_TEXT -> {
                val rawText = action.param1.ifBlank { "Compartido desde Atajos de Android" }
                val textToShare = com.example.engine.VariableResolverHelper.resolve(rawText, context)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, textToShare)
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val shareIntent = Intent.createChooser(sendIntent, "Compartir con").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(shareIntent)
                "Menú compartir abierto"
            }

            ActionType.SET_TIMER -> {
                val seconds = action.param1.toIntOrNull() ?: 60
                val rawLabel = action.param2.ifBlank { "Temporizador Atajo" }
                val label = com.example.engine.VariableResolverHelper.resolve(rawLabel, context)
                try {
                    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, label)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    "Temporizador de ${seconds}s iniciado"
                } catch (e: Exception) {
                    Toast.makeText(context, "Temporizador de $seconds seg ($label)", Toast.LENGTH_LONG).show()
                    "Temporizador programado ($seconds seg)"
                }
            }

            else -> "Acción no soportada por el despachador de intents"
        }
    }
}
