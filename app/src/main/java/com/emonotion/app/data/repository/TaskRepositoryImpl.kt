package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.TaskDao
import com.emonotion.app.data.local.entities.TaskEntity
import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.model.TaskPriority
import com.emonotion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с задачами
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTasksByDate(date: String): Flow<List<Task>> {
        return taskDao.getTasksByDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }
    
    override fun getIncompleteTasks(): Flow<List<Task>> {
        return taskDao.getIncompleteTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getCompletedTasks(): Flow<List<Task>> {
        return taskDao.getCompletedTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>> {
        return taskDao.getTasksByPriority(priority.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(TaskEntity.fromDomain(task))
    }
    
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(TaskEntity.fromDomain(task))
    }
    
    override suspend fun updateTaskCompletion(id: String, completed: Boolean) {
        taskDao.updateTaskCompletion(id, completed)
    }
    
    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(TaskEntity.fromDomain(task))
    }
    
    override suspend fun deleteTaskById(id: String) {
        taskDao.deleteTaskById(id)
    }
    
    override suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedTasks()
    }
    
    override suspend fun getTasksCount(): Int {
        return taskDao.getTasksCount()
    }
    
    override suspend fun getIncompleteTasksCount(): Int {
        return taskDao.getIncompleteTasksCount()
    }
    
    override suspend fun getTasksCountByDate(date: String): Int {
        return taskDao.getTasksCountByDate(date)
    }
    
    override suspend fun getTasksPaginated(limit: Int, offset: Int): Flow<List<Task>> {
        return taskDao.getTasksPaginated(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getRecentTasks(limit: Int): Flow<List<Task>> {
        return taskDao.getRecentTasks(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
