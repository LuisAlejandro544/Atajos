package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.engine.ActionExecutor
import com.example.engine.ExecutionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.model.ShortcutTrigger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Flurix", appName)
  }

  @Test
  fun `overlay permission helper initial state and mark prompt`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertFalse(com.example.ui.components.OverlayPermissionHelper.hasShownPrompt(context))
    com.example.ui.components.OverlayPermissionHelper.markPromptAsShown(context)
    assertTrue(com.example.ui.components.OverlayPermissionHelper.hasShownPrompt(context))
  }

  @Test
  fun `shortcut triggers parsing and defaults`() {
    assertEquals(ShortcutTrigger.BATTERY_LOW, ShortcutTrigger.fromKey("BATTERY_LOW"))
    assertEquals(ShortcutTrigger.BATTERY_OK, ShortcutTrigger.fromKey("BATTERY_OK"))
    assertEquals(ShortcutTrigger.BATTERY_FULL, ShortcutTrigger.fromKey("BATTERY_FULL"))
    assertEquals(ShortcutTrigger.POWER_CONNECTED, ShortcutTrigger.fromKey("POWER_CONNECTED"))
    assertEquals(ShortcutTrigger.NONE, ShortcutTrigger.fromKey("UNKNOWN_TRIGGER"))
  }

  @Test
  fun `execution status cancellation properties`() {
    val status = ExecutionStatus(
      isRunning = false,
      shortcutId = 1L,
      shortcutTitle = "Test Shortcut",
      isFinished = true,
      isSuccess = false,
      isCancelled = true,
      resultMessage = "Cancelado por el usuario"
    )
    assertTrue(status.isCancelled)
    assertFalse(status.isSuccess)
    assertTrue(status.isFinished)
    assertEquals("Cancelado por el usuario", status.resultMessage)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `action executor can be cancelled`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val executor = ActionExecutor(context)

    val actions = listOf(
      ShortcutAction(id = "1", type = ActionType.WAIT_DELAY, title = "Pausa 1", param1 = "1"),
      ShortcutAction(id = "2", type = ActionType.WAIT_DELAY, title = "Pausa 2", param1 = "1"),
      ShortcutAction(id = "3", type = ActionType.WAIT_DELAY, title = "Pausa 3", param1 = "1")
    )

    var finishedSuccess = true
    var finishedMessage = ""

    val job = launch {
      executor.executeShortcut(1L, "Atajo de Prueba", actions) { success, msg, _ ->
        finishedSuccess = success
        finishedMessage = msg
      }
    }

    // Cancel execution immediately
    executor.cancelExecution()
    job.join()

    val currentStatus = executor.status.value
    assertTrue(currentStatus.isCancelled)
    assertFalse(finishedSuccess)
    assertEquals("Cancelado por el usuario", finishedMessage)
  }
}
