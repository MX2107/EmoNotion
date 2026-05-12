package com.emonotion.app.domain.usecase.task

import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use Case для переключения статуса выполнения задачи
 */
class ToggleTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, completed: Boolean): Result<Unit> {
        return try {
            taskRepository.updateTaskCompletion(taskId, completed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend operator fun invoke(task: Task): Result<Unit> {
        return try {
            taskRepository.updateTaskCompletion(task.id, !task.isCompleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
