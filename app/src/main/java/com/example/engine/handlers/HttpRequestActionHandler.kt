package com.example.engine.handlers

import android.content.Context
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.engine.VariableResolverHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manejador para solicitudes HTTP / Webhooks ("Obtener contenido de URL").
 * Permite consultar APIs REST o disparar Webhooks de automatización con GET, POST, PUT, DELETE.
 * Guarda el resultado para que acciones subsiguientes puedan usarlo mediante {RESPUESTA_WEB}.
 */
class HttpRequestActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(ActionType.HTTP_REQUEST)

    override suspend fun execute(action: ShortcutAction): String = withContext(Dispatchers.IO) {
        var rawUrl = action.param1.trim().ifBlank { "https://httpbin.org/get" }
        // Resolver variables del sistema en la URL si contiene tags
        rawUrl = VariableResolverHelper.resolve(rawUrl, context)

        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            rawUrl = "https://$rawUrl"
        }

        val method = action.param2.trim().uppercase().ifBlank { "GET" }
        var body = action.param3
        if (body.isNotBlank()) {
            body = VariableResolverHelper.resolve(body, context)
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(rawUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 12000
                readTimeout = 12000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Atajos-Android/1.0")
                setRequestProperty("Accept", "application/json, text/plain, */*")

                if (method in listOf("POST", "PUT", "PATCH")) {
                    doOutput = true
                    if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    } else {
                        setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
                    }
                    OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                        writer.write(body)
                        writer.flush()
                    }
                }
            }

            val responseCode = connection.responseCode
            val isSuccess = responseCode in 200..299

            val inputStream = if (isSuccess) connection.inputStream else (connection.errorStream ?: connection.inputStream)
            val responseText = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                val sb = StringBuilder()
                var line: String?
                var totalChars = 0
                while (reader.readLine().also { line = it } != null) {
                    sb.appendLine(line)
                    totalChars += (line?.length ?: 0)
                    if (totalChars > 4000) {
                        sb.appendLine("...[Respuesta truncada]")
                        break
                    }
                }
                sb.toString().trim()
            }

            // Guardar en caché para que siguientes acciones puedan usar {RESPUESTA_WEB} y {HTTP_STATUS}
            VariableResolverHelper.saveLastWebResponse(
                response = responseText.ifBlank { "Respuesta vacía (HTTP $responseCode)" },
                status = responseCode.toString()
            )

            val preview = if (responseText.length > 80) {
                responseText.take(77).replace("\n", " ") + "..."
            } else {
                responseText.replace("\n", " ")
            }

            if (isSuccess) {
                "HTTP $responseCode: $preview"
            } else {
                "HTTP Error $responseCode: $preview"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = e.localizedMessage ?: "Fallo de conexión"
            VariableResolverHelper.saveLastWebResponse(
                response = "Error: $errorMsg",
                status = "500"
            )
            "Error en petición HTTP: $errorMsg"
        } finally {
            connection?.disconnect()
        }
    }
}
