package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.CustomMoodEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с пользовательскими настроениями
 */
@Dao
interface CustomMoodDao {
    
    @Query("SELECT * FROM custom_moods WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveCustomMoods(): Flow<List<CustomMoodEntity>>
    
    @Query("SELECT * FROM custom_moods ORDER BY name ASC")
    fun getAllCustomMoods(): Flow<List<CustomMoodEntity>>
    
    @Query("SELECT * FROM custom_moods WHERE id = :id")
    suspend fun getCustomMoodById(id: String): CustomMoodEntity?
    
    @Query("SELECT * FROM custom_moods WHERE name = :name")
    suspend fun getCustomMoodByName(name: String): CustomMoodEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomMood(customMood: CustomMoodEntity)
    
    @Update
    suspend fun updateCustomMood(customMood: CustomMoodEntity)
    
    @Delete
    suspend fun deleteCustomMood(customMood: CustomMoodEntity)
    
    @Query("UPDATE custom_moods SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun deactivateCustomMood(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE custom_moods SET isActive = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun activateCustomMood(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM custom_moods WHERE id = :id")
    suspend fun deleteCustomMoodById(id: String)
    
    @Query("DELETE FROM custom_moods")
    suspend fun deleteAllCustomMoods()
    
    @Query("SELECT * FROM custom_moods")
    suspend fun getAllCustomMoodsList(): List<CustomMoodEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomMoods(customMoods: List<CustomMoodEntity>)
}
