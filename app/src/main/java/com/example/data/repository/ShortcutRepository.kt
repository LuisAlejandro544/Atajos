package com.example.data.repository

import com.example.data.db.ShortcutDao
import com.example.data.model.ShortcutEntity
import kotlinx.coroutines.flow.Flow

class ShortcutRepository(private val dao: ShortcutDao) {
    val allShortcuts: Flow<List<ShortcutEntity>> = dao.getAllShortcuts()
    val favoriteShortcuts: Flow<List<ShortcutEntity>> = dao.getFavoriteShortcuts()

    suspend fun insert(shortcut: ShortcutEntity): Long = dao.insert(shortcut)

    suspend fun update(shortcut: ShortcutEntity) = dao.update(shortcut)

    suspend fun delete(shortcut: ShortcutEntity) = dao.delete(shortcut)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = dao.updateFavorite(id, isFavorite)

    suspend fun incrementExecution(id: Long) = dao.incrementExecutionCount(id)

    suspend fun resetDefaults(defaultShortcuts: List<ShortcutEntity>) {
        dao.deleteAll()
        dao.insertAll(defaultShortcuts)
    }
}
