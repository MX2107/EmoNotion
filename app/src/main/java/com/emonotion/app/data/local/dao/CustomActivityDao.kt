package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.CustomActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с пользовательскими занятиями/активностями
 */
@Dao
interface CustomActivityDao {
    
    @Query("SELECT * FROM custom_activities WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveCustomActivities(): Flow<List<CustomActivityEntity>>
    
    @Query("SELECT * FROM custom_activities ORDER BY name ASC")
    fun getAllCustomActivities(): Flow<List<CustomActivityEntity>>
    
    @Query("SELECT * FROM custom_activities WHERE id = :id")
    suspend fun getCustomActivityById(id: String): CustomActivityEntity?
    
    @Query("SELECT * FROM custom_activities WHERE name = :name")
    suspend fun getCustomActivityByName(name: String): CustomActivityEntity?
    
    @Query("SELECT * FROM custom_activities WHERE category = :category AND isActive = 1 ORDER BY name ASC")
    fun getCustomActivitiesByCategory(category: String): Flow<List<CustomActivityEntity>>
    
    @Query("SELECT DISTINCT category FROM custom_activities WHERE isActive = 1 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomActivity(customActivity: CustomActivityEntity)
    
    @Update
    suspend fun updateCustomActivity(customActivity: CustomActivityEntity)
    
    @Delete
    suspend fun deleteCustomActivity(customActivity: CustomActivityEntity)
    
    @Query("UPDATE custom_activities SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun deactivateCustomActivity(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE custom_activities SET isActive = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun activateCustomActivity(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM custom_activities WHERE id = :id")
    suspend fun deleteCustomActivityById(id: String)
    
    @Query("DELETE FROM custom_activities")
    suspend fun deleteAllCustomActivities()
    
    @Query("SELECT * FROM custom_activities")
    suspend fun getAllCustomActivitiesList(): List<CustomActivityEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomActivities(customActivities: List<CustomActivityEntity>)
}
