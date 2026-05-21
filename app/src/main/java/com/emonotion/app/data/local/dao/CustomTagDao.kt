package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.CustomTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с пользовательскими тегами
 */
@Dao
interface CustomTagDao {
    
    @Query("SELECT * FROM custom_tags WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveCustomTags(): Flow<List<CustomTagEntity>>
    
    @Query("SELECT * FROM custom_tags ORDER BY name ASC")
    fun getAllCustomTags(): Flow<List<CustomTagEntity>>
    
    @Query("SELECT * FROM custom_tags WHERE id = :id")
    suspend fun getCustomTagById(id: String): CustomTagEntity?
    
    @Query("SELECT * FROM custom_tags WHERE name = :name")
    suspend fun getCustomTagByName(name: String): CustomTagEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTag(customTag: CustomTagEntity)
    
    @Update
    suspend fun updateCustomTag(customTag: CustomTagEntity)
    
    @Delete
    suspend fun deleteCustomTag(customTag: CustomTagEntity)
    
    @Query("UPDATE custom_tags SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun deactivateCustomTag(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE custom_tags SET isActive = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun activateCustomTag(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM custom_tags WHERE id = :id")
    suspend fun deleteCustomTagById(id: String)
    
    @Query("DELETE FROM custom_tags")
    suspend fun deleteAllCustomTags()
    
    @Query("SELECT * FROM custom_tags")
    suspend fun getAllCustomTagsList(): List<CustomTagEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTags(customTags: List<CustomTagEntity>)
}
