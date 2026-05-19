package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с записями о настроении
 */
@Dao
interface MoodDao {
    
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntryEntity>>
    
    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    suspend fun getMoodByDate(date: String): MoodEntryEntity?
    
    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    fun getMoodByDateFlow(date: String): Flow<MoodEntryEntity?>
    
    @Query("SELECT * FROM mood_entries WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getMoodsByDateRange(from: Long, to: Long): Flow<List<MoodEntryEntity>>
    
    @Query("SELECT * FROM mood_entries WHERE date >= :startDate AND date <= :endDate ORDER BY timestamp DESC")
    fun getMoodsByDateRange(startDate: String, endDate: String): Flow<List<MoodEntryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<MoodEntryEntity>)
    
    @Update
    suspend fun updateMood(mood: MoodEntryEntity)
    
    @Delete
    suspend fun deleteMood(mood: MoodEntryEntity)
    
    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodById(id: String)
    
    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoods()
    
    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getMoodsCount(): Int
    
    @Query("SELECT COUNT(*) FROM mood_entries WHERE date = :date")
    suspend fun hasMoodForDate(date: String): Boolean
    
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    suspend fun getAllMoodsList(): List<MoodEntryEntity>
}
