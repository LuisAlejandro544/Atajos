package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY id ASC")
    fun getAllShortcuts(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE category = :category ORDER BY id ASC")
    fun getShortcutsByCategory(category: String): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE trigger = :trigger OR trigger = 'POWER_BOTH' ORDER BY id ASC")
    suspend fun getShortcutsForPowerTrigger(trigger: String): List<ShortcutEntity>

    @Query("SELECT * FROM shortcuts WHERE trigger != 'NONE' ORDER BY id ASC")
    fun getAutomatedShortcuts(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE id = :id")
    suspend fun getShortcutById(id: Long): ShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: ShortcutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shortcuts: List<ShortcutEntity>)

    @Update
    suspend fun updateShortcut(shortcut: ShortcutEntity)

    @Delete
    suspend fun deleteShortcut(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE shortcuts SET runCount = runCount + 1, lastRunTimestamp = :timestamp WHERE id = :id")
    suspend fun recordExecution(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE shortcuts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM shortcuts")
    suspend fun getCount(): Int
}
