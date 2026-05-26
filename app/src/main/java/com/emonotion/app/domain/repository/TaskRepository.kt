package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с задачами
 */
interface TaskRepository {
    
    /**
     * Получить все задачи
     */
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Получить задачи за конкретную дату
     */
    fun getTasksByDate(date: String): Flow<List<Task>>
    
    /**
     * Получить задачу по ID
     */
    suspend fun getTaskById(id: String): Task?
    
    /**
     * Получить незавершенные задачи
     */
    fun getIncompleteTasks(): Flow<List<Task>>
    
    /**
     * Получить завершенные задачи
     */
    fun getCompletedTasks(): Flow<List<Task>>
    
    /**
     * Получить задачи по приоритету
     */
    fun getTasksByPriority(priority: com.emonotion.app.domain.model.TaskPriority): Flow<List<Task>>
    
    /**
     * Поиск задач по тексту
     */
    fun searchTasks(query: String): Flow<List<Task>>
    
    /**
     * Добавить новую задачу
     */
    suspend fun insertTask(task: Task)
    
    /**
     * Обновить существующую задачу
     */
    suspend fun updateTask(task: Task)
    
    /**
     * Изменить статус выполнения задачи
     */
    suspend fun updateTaskCompletion(id: String, completed: Boolean)
    
    /**
     * Удалить задачу
     */
    suspend fun deleteTask(task: Task)
    
    /**
     * Удалить задачу по ID
     */
    suspend fun deleteTaskById(id: String)
    
    /**
     * Удалить все завершенные задачи
     */
    suspend fun deleteCompletedTasks()
    
    /**
     * Получить количество всех задач
     */
    suspend fun getTasksCount(): Int
    
    /**
     * Получить количество незавершенных задач
     */
    suspend fun getIncompleteTasksCount(): Int
    
    /**
     * Получить количество задач за дату
     */
    suspend fun getTasksCountByDate(date: String): Int
}
