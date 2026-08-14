package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.DefaultShortcutsProvider
import com.example.data.model.AutomationEntity
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ShortcutEntity
import kotlinx.coroutines.flow.Flow

class ShortcutRepository(private val database: AppDatabase) {
    private val shortcutDao = database.shortcutDao()
    private val automationDao = database.automationDao()
    private val executionLogDao = database.executionLogDao()

    val allShortcuts: Flow<List<ShortcutEntity>> = shortcutDao.getAllShortcuts()
    val allAutomations: Flow<List<AutomationEntity>> = automationDao.getAllAutomations()
    val recentLogs: Flow<List<ExecutionLogEntity>> = executionLogDao.getRecentLogs()

    suspend fun checkAndSeedDefaults() {
        val count = shortcutDao.getCount()
        if (count == 0) {
            shortcutDao.insertAll(DefaultShortcutsProvider.getDefaultShortcuts())
        }
        val autoCount = automationDao.getCount()
        if (autoCount == 0) {
            automationDao.insertAll(DefaultShortcutsProvider.getDefaultAutomations())
        }
    }

    suspend fun getShortcutById(id: Long): ShortcutEntity? = shortcutDao.getShortcutById(id)

    suspend fun saveShortcut(shortcut: ShortcutEntity): Long {
        return if (shortcut.id == 0L) {
            shortcutDao.insertShortcut(shortcut)
        } else {
            shortcutDao.updateShortcut(shortcut)
            shortcut.id
        }
    }

    suspend fun deleteShortcut(id: Long) {
        shortcutDao.deleteById(id)
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) {
        shortcutDao.setFavorite(id, !current)
    }

    suspend fun recordExecution(id: Long) {
        shortcutDao.recordExecution(id)
    }

    suspend fun logExecution(log: ExecutionLogEntity) {
        executionLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        executionLogDao.clearAll()
    }

    suspend fun saveAutomation(automation: AutomationEntity): Long {
        return if (automation.id == 0L) {
            automationDao.insertAutomation(automation)
        } else {
            automationDao.updateAutomation(automation)
            automation.id
        }
    }

    suspend fun toggleAutomation(id: Long, enabled: Boolean) {
        automationDao.toggleAutomation(id, enabled)
    }

    suspend fun deleteAutomation(id: Long) {
        automationDao.deleteById(id)
    }
}
