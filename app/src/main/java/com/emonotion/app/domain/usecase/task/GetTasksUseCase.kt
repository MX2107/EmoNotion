package com.emonotion.app.domain.usecase.task

import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения задач
 */
class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getAllTasks()
    }
    
    suspend operator fun invoke(date: String): Flow<List<Task>> {
        return taskRepository.getTasksByDate(date)
    }
    
    suspend operator fun invoke(completed: Boolean): Flow<List<Task>> {
        return if (completed) {
            taskRepository.getCompletedTasks()
        } else {
            taskRepository.getIncompleteTasks()
        }
    }
    
    suspend fun getTasksPaginated(limit: Int, offset: Int): Flow<List<Task>> {
        return taskRepository.getTasksPaginated(limit, offset)
    }
    
    suspend fun getRecentTasks(limit: Int): Flow<List<Task>> {
        return taskRepository.getRecentTasks(limit)
    }
}
