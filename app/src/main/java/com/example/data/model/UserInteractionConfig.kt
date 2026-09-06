package com.example.data.model

import org.json.JSONObject

data class UserInteractionConfig(
    val designType: String = DESIGN_MODAL, // "MODAL" o "NOTIFICATION"
    val promptTitle: String = "¿Deseas continuar?",
    val promptMessage: String = "Confirma para ejecutar los siguientes pasos del atajo.",
    val inputMode: String = MODE_BUTTONS, // "BUTTONS" o "KEYWORD"
    val expectedKeyword: String = "Si",
    val buttonPositiveText: String = "Continuar",
    val buttonNegativeText: String = "Cancelar"
) {
    companion object {
        const val DESIGN_MODAL = "MODAL"
        const val DESIGN_NOTIFICATION = "NOTIFICATION"
        const val MODE_BUTTONS = "BUTTONS"
        const val MODE_KEYWORD = "KEYWORD"

        fun fromJson(raw: String): UserInteractionConfig {
            if (raw.isBlank()) return UserInteractionConfig()
            return try {
                val json = JSONObject(raw)
                UserInteractionConfig(
                    designType = json.optString("designType", DESIGN_MODAL),
                    promptTitle = json.optString("promptTitle", "¿Deseas continuar?"),
                    promptMessage = json.optString("promptMessage", "Confirma para ejecutar los siguientes pasos del atajo."),
                    inputMode = json.optString("inputMode", MODE_BUTTONS),
                    expectedKeyword = json.optString("expectedKeyword", "Si"),
                    buttonPositiveText = json.optString("buttonPositiveText", "Continuar"),
                    buttonNegativeText = json.optString("buttonNegativeText", "Cancelar")
                )
            } catch (_: Exception) {
                UserInteractionConfig()
            }
        }
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("designType", designType)
        json.put("promptTitle", promptTitle)
        json.put("promptMessage", promptMessage)
        json.put("inputMode", inputMode)
        json.put("expectedKeyword", expectedKeyword)
        json.put("buttonPositiveText", buttonPositiveText)
        json.put("buttonNegativeText", buttonNegativeText)
        return json.toString()
    }
}
