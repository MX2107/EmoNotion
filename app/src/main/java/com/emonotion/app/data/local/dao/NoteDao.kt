package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с заметками
 */
@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAllNotesList(): List<NoteEntity>
    
    @Query("SELECT * FROM notes WHERE date = :date ORDER BY isPinned DESC, timestamp DESC")
    fun getNotesByDate(date: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?
    
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    fun getNotesByTag(tag: String): Flow<List<NoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)
    
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
    
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int
    
    @Query("SELECT COUNT(*) FROM notes WHERE date = :date")
    suspend fun getNotesCountByDate(date: String): Int
    
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC LIMIT :limit OFFSET :offset")
    fun getNotesPaginated(limit: Int, offset: Int): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC LIMIT :limit")
    fun getRecentNotes(limit: Int): Flow<List<NoteEntity>>
}
