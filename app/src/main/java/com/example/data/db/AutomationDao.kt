package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AutomationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automations ORDER BY id DESC")
    fun getAllAutomations(): Flow<List<AutomationEntity>>

    @Query("SELECT * FROM automations WHERE isEnabled = 1")
    fun getActiveAutomations(): Flow<List<AutomationEntity>>

    @Query("SELECT * FROM automations WHERE isEnabled = 1")
    suspend fun getActiveAutomationsList(): List<AutomationEntity>

    @Query("SELECT * FROM automations WHERE isEnabled = 1 AND triggerType = :triggerType")
    suspend fun getActiveAutomationsByTriggerType(triggerType: com.example.data.model.TriggerType): List<AutomationEntity>

    @Query("SELECT * FROM automations WHERE isEnabled = 1 AND (triggerType = :triggerType OR triggerType = 'CHARGER_BOTH')")
    suspend fun getActiveAutomationsForPowerTrigger(triggerType: com.example.data.model.TriggerType): List<AutomationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomation(automation: AutomationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(automations: List<AutomationEntity>)

    @Update
    suspend fun updateAutomation(automation: AutomationEntity)

    @Query("UPDATE automations SET isEnabled = :enabled WHERE id = :id")
    suspend fun toggleAutomation(id: Long, enabled: Boolean)

    @Delete
    suspend fun deleteAutomation(automation: AutomationEntity)

    @Query("SELECT * FROM automations ORDER BY id DESC")
    suspend fun getAllAutomationsSync(): List<AutomationEntity>

    @Query("SELECT * FROM automations WHERE id = :id")
    suspend fun getAutomationById(id: Long): AutomationEntity?

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM automations")
    suspend fun getCount(): Int
}
