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
    @Query("SELECT * FROM shortcuts ORDER BY isFavorite DESC, id ASC")
    fun getAllShortcuts(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteShortcuts(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE category = :category ORDER BY isFavorite DESC, id ASC")
    fun getShortcutsByCategory(category: String): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE id = :id LIMIT 1")
    suspend fun getShortcutById(id: Long): ShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: ShortcutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shortcuts: List<ShortcutEntity>)

    @Update
    suspend fun update(shortcut: ShortcutEntity)

    @Delete
    suspend fun delete(shortcut: ShortcutEntity)

    @Query("UPDATE shortcuts SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE shortcuts SET executionCount = executionCount + 1 WHERE id = :id")
    suspend fun incrementExecutionCount(id: Long)

    @Query("DELETE FROM shortcuts")
    suspend fun deleteAll()
}
