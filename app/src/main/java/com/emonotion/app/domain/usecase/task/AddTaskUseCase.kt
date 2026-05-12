package com.emonotion.app.domain.usecase.task

import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use Case для добавления задачи
 */
class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return try {
            taskRepository.insertTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
