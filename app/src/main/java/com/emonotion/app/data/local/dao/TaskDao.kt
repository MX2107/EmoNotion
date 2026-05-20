package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с задачами
 */
@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY priority DESC, timestamp ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY timestamp ASC")
    suspend fun getAllTasksList(): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY priority DESC, timestamp ASC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, timestamp ASC")
    fun getIncompleteTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY timestamp ASC")
    fun getTasksByPriority(priority: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskCompletion(id: String, completed: Boolean)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
    
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTasksCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getIncompleteTasksCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date")
    suspend fun getTasksCountByDate(date: String): Int
}
