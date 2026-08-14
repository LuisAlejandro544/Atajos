package com.example.engine.handlers

import android.content.Context
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import kotlinx.coroutines.delay

class UtilityActionHandler(private val context: Context) : ActionHandler {

    override val supportedTypes: Set<ActionType> = setOf(
        ActionType.WAIT_DELAY,
        ActionType.QUICK_CALCULATOR
    )

    override suspend fun execute(action: ShortcutAction): String {
        return when (action.type) {
            ActionType.WAIT_DELAY -> {
                val seconds = action.param1.toIntOrNull() ?: 1
                delay(seconds * 1000L)
                "Pausa de ${seconds}s completada"
            }

            ActionType.QUICK_CALCULATOR -> {
                val amount = action.param1.toDoubleOrNull() ?: 50.0
                val percentage = action.param2.toDoubleOrNull() ?: 15.0
                val calculated = (amount * percentage) / 100.0
                val total = amount + calculated
                val resultText = "Cuenta: $$amount | Propina ($percentage%): $$calculated | Total: $$total"
                Toast.makeText(context, resultText, Toast.LENGTH_LONG).show()
                resultText
            }

            else -> "Acción de utilidad no soportada"
        }
    }
}
