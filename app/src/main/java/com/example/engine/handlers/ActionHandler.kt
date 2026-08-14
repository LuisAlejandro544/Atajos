package com.example.engine.handlers

import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction

/**
 * Contrato base para los manejadores de acciones individuales en el motor de ejecución.
 * Cada manejador encapsula la lógica e interacción con APIs del sistema Android específicas.
 */
interface ActionHandler {
    val supportedTypes: Set<ActionType>
    suspend fun execute(action: ShortcutAction): String
    fun onCancelled() {}
    fun release() {}
}
