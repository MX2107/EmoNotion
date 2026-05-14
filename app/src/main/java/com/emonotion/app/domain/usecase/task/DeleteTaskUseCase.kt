package com.emonotion.app.domain.usecase.task

import com.emonotion.app.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use Case для удаления задачи
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        return try {
            taskRepository.deleteTaskById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
