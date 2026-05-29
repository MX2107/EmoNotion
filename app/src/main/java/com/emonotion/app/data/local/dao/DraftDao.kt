package com.emonotion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.emonotion.app.data.local.entities.DraftEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с черновиками записей о настроении
 */
@Dao
interface DraftDao {
    
    @Query("SELECT * FROM draft_entries WHERE date = :date LIMIT 1")
    suspend fun getDraftByDate(date: String): DraftEntity?
    
    @Query("SELECT * FROM draft_entries WHERE date = :date LIMIT 1")
    fun getDraftByDateFlow(date: String): Flow<DraftEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftEntity)
    
    @Delete
    suspend fun deleteDraft(draft: DraftEntity)
    
    @Query("DELETE FROM draft_entries WHERE date = :date")
    suspend fun deleteDraftByDate(date: String)
    
    @Query("DELETE FROM draft_entries")
    suspend fun deleteAllDrafts()
    
    @Query("SELECT * FROM draft_entries ORDER BY updatedAt DESC")
    fun getAllDrafts(): Flow<List<DraftEntity>>
}
