package com.example

import com.example.data.model.ShortcutTrigger
import com.example.engine.triggers.TimeSchedulerHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBatteryExactTriggerKeyAndParsing() {
        val key = ShortcutTrigger.buildBatteryExactKey(80)
        assertEquals("BATTERY_EXACT:80", key)
        assertEquals(80, ShortcutTrigger.getBatteryExactLevel(key))
        assertEquals(ShortcutTrigger.BATTERY_EXACT, ShortcutTrigger.fromKey(key))

        val key2 = ShortcutTrigger.buildBatteryExactKey(15)
        assertEquals(15, ShortcutTrigger.getBatteryExactLevel(key2))
    }

    @Test
    fun testTimeExactTriggerKeyAndParsing() {
        val key = ShortcutTrigger.buildTimeExactKey("08:30")
        assertEquals("TIME_EXACT:08:30", key)
        assertEquals("08:30", ShortcutTrigger.getTimeExactValue(key))
        assertEquals(ShortcutTrigger.TIME_EXACT, ShortcutTrigger.fromKey(key))

        val parsed = TimeSchedulerHelper.parseHourMinute("08:30")
        assertNotNull(parsed)
        assertEquals(8, parsed?.first)
        assertEquals(30, parsed?.second)

        val formatted = TimeSchedulerHelper.formatTimeString(8, 30)
        assertEquals("08:30", formatted)
    }

    @Test
    fun testTimeParsingEdgeCases() {
        val p1 = TimeSchedulerHelper.parseHourMinute("23:59")
        assertEquals(Pair(23, 59), p1)

        val p2 = TimeSchedulerHelper.parseHourMinute("00:00")
        assertEquals(Pair(0, 0), p2)

        val p3 = TimeSchedulerHelper.parseHourMinute("invalid")
        assertNull(p3)
    }
}
