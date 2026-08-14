package com.example.engine.tiles

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import com.example.MainActivity
import com.example.data.db.AppDatabase
import com.example.data.model.ActionJsonHelper
import com.example.data.model.ExecutionLogEntity
import com.example.data.repository.ShortcutRepository
import com.example.engine.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Servicio de Mosaico en la cortina de Ajustes Rápidos (Quick Settings Tile).
 * Permite al usuario disparar su atajo favorito o más reciente desde cualquier pantalla
 * o incluso con el dispositivo en la pantalla de bloqueo con un solo toque.
 */
class ShortcutTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        scope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val shortcutsFlow = db.shortcutDao().getAllShortcuts()
                val shortcutList = shortcutsFlow.firstOrNull() ?: emptyList()
                val targetShortcut = shortcutList.firstOrNull { it.isFavorite } ?: shortcutList.firstOrNull()

                withContext(Dispatchers.Main) {
                    if (targetShortcut != null) {
                        tile.label = targetShortcut.title
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            tile.subtitle = "Tocar para ejecutar"
                        }
                        tile.state = Tile.STATE_INACTIVE
                    } else {
                        tile.label = "Atajos"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            tile.subtitle = "Sin atajos"
                        }
                        tile.state = Tile.STATE_UNAVAILABLE
                    }
                    tile.updateTile()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando estado del Quick Settings Tile", e)
            }
        }
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        scope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val repository = ShortcutRepository(db)
                val shortcuts = repository.allShortcuts.firstOrNull() ?: emptyList()
                val targetShortcut = shortcuts.firstOrNull { it.isFavorite } ?: shortcuts.firstOrNull()

                if (targetShortcut == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "No hay atajos configurados en la biblioteca", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivityAndCollapse(intent)
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    tile.state = Tile.STATE_ACTIVE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.subtitle = "Ejecutando..."
                    }
                    tile.updateTile()
                }

                val actions = ActionJsonHelper.fromJson(targetShortcut.actionsJson)
                val actionExecutor = ActionExecutor(applicationContext)

                actionExecutor.executeShortcut(
                    shortcutId = targetShortcut.id,
                    shortcutTitle = targetShortcut.title,
                    actions = actions
                ) { success, resultMessage, durationMs ->
                    scope.launch {
                        if (success) {
                            repository.recordExecution(targetShortcut.id)
                        }
                        repository.logExecution(
                            ExecutionLogEntity(
                                shortcutId = targetShortcut.id,
                                shortcutTitle = targetShortcut.title,
                                iconKey = targetShortcut.iconKey,
                                colorHex = targetShortcut.colorHex,
                                timestamp = System.currentTimeMillis(),
                                status = if (success) "SUCCESS" else "FAILED",
                                actionCount = actions.size,
                                durationMs = durationMs,
                                summary = "[Ajuste Rápido / Tile] $resultMessage"
                            )
                        )

                        withContext(Dispatchers.Main) {
                            tile.state = Tile.STATE_INACTIVE
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                tile.subtitle = if (success) "Completado" else "Fallido"
                            }
                            tile.updateTile()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ejecutando atajo desde Quick Settings Tile", e)
                withContext(Dispatchers.Main) {
                    tile.state = Tile.STATE_INACTIVE
                    tile.updateTile()
                }
            }
        }
    }

    companion object {
        private const val TAG = "ShortcutTileService"
    }
}
