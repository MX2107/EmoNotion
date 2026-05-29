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
import kotlinx.coroutines.flow.first
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<FilterStatus>(FilterStatus.ALL)
    val filterStatus: StateFlow<FilterStatus> = _filterStatus.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private var tasksCollectionJob: kotlinx.coroutines.Job? = null
    
    enum class FilterStatus {
        ALL, ACTIVE, COMPLETED
    }

    enum class SortOrder {
        NEWEST_FIRST, OLDEST_FIRST, ALPHABETICAL_ASC, ALPHABETICAL_DESC
    }

    init {
        // Загружаем задачи при создании ViewModel
        loadTasks()
    }

    /**
     * Загружает задачи
     */
    fun loadTasks() {
        viewModelScope.launch {
            getTasksUseCase().collect { tasksList ->
                _tasks.value = filterAndSortTasks(tasksList)
            }
        }
    }
    
    /**
     * Выполняет поиск задач
     */
    fun searchTasks(query: String) {
        _searchQuery.value = query
        loadTasks()
    }
    
    /**
     * Устанавливает фильтр по статусу
     */
    fun setFilterStatus(status: FilterStatus) {
        _filterStatus.value = status
        loadTasks()
    }
    
    /**
     * Устанавливает порядок сортировки
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        loadTasks()
    }
    
    /**
     * Фильтрует и сортирует задачи
     */
    private fun filterAndSortTasks(tasks: List<Task>): List<Task> {
        var result = tasks
        
        // Фильтрация по поиску
        if (_searchQuery.value.isNotEmpty()) {
            result = result.filter { 
                it.title.contains(_searchQuery.value, ignoreCase = true) ||
                (it.description?.contains(_searchQuery.value, ignoreCase = true) ?: false)
            }
        }
        
        // Фильтрация по статусу
        result = when (_filterStatus.value) {
            FilterStatus.ACTIVE -> result.filter { !it.isCompleted }
            FilterStatus.COMPLETED -> result.filter { it.isCompleted }
            FilterStatus.ALL -> result
        }
        
        // Сортировка
        result = when (_sortOrder.value) {
            SortOrder.NEWEST_FIRST -> result.sortedByDescending { it.timestamp }
            SortOrder.OLDEST_FIRST -> result.sortedBy { it.timestamp }
            SortOrder.ALPHABETICAL_ASC -> result.sortedBy { it.title.lowercase() }
            SortOrder.ALPHABETICAL_DESC -> result.sortedByDescending { it.title.lowercase() }
        }
        
        return result
    }
    
    /**
     * Добавляет новую задачу
     */
    fun addTask(title: String, description: String? = null) {
        executeWithResult(
            operation = {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = if (description.isNullOrBlank()) null else description,
                    timestamp = System.currentTimeMillis(),
                    date = today,
                    isCompleted = false,
                    priority = TaskPriority.MEDIUM
                )
                addTaskUseCase(newTask)
            },
            onSuccess = {
                loadTasks()
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
