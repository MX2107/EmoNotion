package com.emonotion.app.presentation.notes

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.model.TaskPriority
import com.emonotion.app.domain.usecase.task.AddTaskUseCase
import com.emonotion.app.domain.usecase.task.DeleteTaskUseCase
import com.emonotion.app.domain.usecase.task.GetTasksUseCase
import com.emonotion.app.domain.usecase.task.UpdateTaskUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel для экрана задач
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase
) : BaseViewModel() {
    
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Состояния UI
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    /**
     * Загружает задачи
     */
    fun loadTasks() {
        executeWithLoading {
            viewModelScope.launch {
                getTasksUseCase().collect { tasksList ->
                    _tasks.value = tasksList.filter { it.date == today }
                }
            }
        }
    }
    
    /**
     * Добавляет новую задачу
     */
    fun addTask(title: String) {
        executeWithResult(
            operation = {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = null,
                    timestamp = System.currentTimeMillis(),
                    date = today,
                    isCompleted = false,
                    priority = TaskPriority.MEDIUM
                )
                addTaskUseCase(newTask)
            },
            onSuccess = {
                loadTasks() // Обновляем список после добавления
            }
        )
    }
    
    /**
     * Переключает статус задачи
     */
    fun toggleTaskCompletion(task: Task) {
        executeWithResult(
            operation = {
                val updatedTask = task.copy(
                    isCompleted = !task.isCompleted
                )
                updateTaskUseCase(updatedTask)
            },
            onSuccess = {
                loadTasks() // Обновляем список после изменения
            }
        )
    }
    
    /**
     * Удаляет задачу
     */
    fun deleteTask(taskId: String) {
        executeWithResult(
            operation = {
                deleteTaskUseCase(taskId)
            },
            onSuccess = {
                loadTasks()
            }
        )
    }
    
    /**
     * Удаляет задачу
     */
    fun deleteTask(task: Task) {
        deleteTask(task.id)
    }
    
    /**
     * Обновляет задачу
     */
    fun updateTask(task: Task) {
        executeWithResult(
            operation = {
                updateTaskUseCase(task)
            },
            onSuccess = {
                loadTasks()
            }
        )
    }
    
    /**
     * Обновляет данные
     */
    fun refreshTasks() {
        loadTasks()
    }
}
