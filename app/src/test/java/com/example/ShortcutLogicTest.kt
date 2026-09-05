package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.ShortcutEntity
import com.example.executor.ShortcutExecutor
import com.example.ui.components.ShortcutIconHelper
import com.example.ui.components.parseColorSafe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutLogicTest {

    @Test
    fun testActionTypeDefaults() {
        val flashlight = ActionType.FLASHLIGHT
        assertEquals("Alternar Linterna", flashlight.label)

        val openUrl = ActionType.OPEN_URL
        assertEquals("https://google.com", openUrl.defaultParam)

        val timer = ActionType.SET_TIMER
        assertEquals("5", timer.defaultParam)

        val lua = ActionType.LUA_SCRIPT
        assertEquals("Script en Lua", lua.label)
        assertTrue(lua.defaultParam.contains("get_hour"))

        val wait = ActionType.WAIT
        assertEquals("Esperar (Pausa)", wait.label)
        assertEquals("1003", wait.defaultParam)

        val speak = ActionType.SPEAK
        assertEquals("Texto a Voz", speak.label)
        assertTrue(speak.paramLabel.contains("voz"))
    }

    @Test
    fun testMultiBlockShortcutEntity() {
        val blocks = listOf(
            ActionBlock(actionType = ActionType.FLASHLIGHT.name, parameter = "", customLabel = "Encender Flash"),
            ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "Alerta enviada", customLabel = "Copiar aviso"),
            ActionBlock(actionType = ActionType.LUA_SCRIPT.name, parameter = "return 42", customLabel = "Cálculo Lua")
        )

        val shortcut = ShortcutEntity(
            title = "Secuencia Alerta",
            description = "3 acciones consecutivas",
            colorHex = "#FF3B30",
            iconKey = "FLASH",
            actionType = ActionType.FLASHLIGHT.name,
            actions = blocks,
            parameter = "",
            isFavorite = true,
            category = "Utilidades"
        )

        assertEquals("Secuencia Alerta", shortcut.title)
        assertEquals(3, shortcut.actions.size)
        assertEquals("Encender Flash", shortcut.actions[0].customLabel)
        assertEquals(ActionType.COPY_TEXT.name, shortcut.actions[1].actionType)
        assertEquals(ActionType.LUA_SCRIPT.name, shortcut.actions[2].actionType)
    }

    @Test
    fun testStepDelayIsExactly1003Ms() {
        // Verifica que la espera entre bloques sea exactamente 1 segundo con 3 milisegundos (1003 ms)
        assertEquals(1003L, ShortcutExecutor.STEP_DELAY_MS)
    }

    @Test
    fun testParseColorSafe() {
        val blue = parseColorSafe("#007AFF")
        assertEquals(Color(0xFF007AFF), blue)

        val invalidFallback = parseColorSafe("invalid_color", Color.Red)
        assertEquals(Color.Red, invalidFallback)
    }

    @Test
    fun testIconHelper() {
        val icon = ShortcutIconHelper.getIcon("FLASH")
        assertNotNull(icon)

        val codeIcon = ShortcutIconHelper.getIcon("CODE")
        assertNotNull(codeIcon)

        val fallbackIcon = ShortcutIconHelper.getIcon("NON_EXISTENT")
        assertNotNull(fallbackIcon)
        assertEquals(ShortcutIconHelper.availableIcons.first().icon, fallbackIcon)
    }
}
