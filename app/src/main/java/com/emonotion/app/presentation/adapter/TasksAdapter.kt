package com.emonotion.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemDateHeaderBinding
import com.emonotion.app.databinding.ItemTaskBinding
import com.emonotion.app.domain.model.Task
import com.emonotion.app.presentation.notes.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Элементы списка задач
 */
sealed class TaskListItem {
    data class DateHeader(val date: String, val formattedDate: String) : TaskListItem()
    data class TaskItem(val task: Task) : TaskListItem()
}

/**
 * Адаптер для списка задач
 */
class TasksAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onItemLongClick: (Task) -> Unit,
    private val onToggleTask: (Task) -> Unit
) : ListAdapter<TaskListItem, RecyclerView.ViewHolder>(TaskListItemDiffCallback()) {

    private var currentSortOrder: TasksViewModel.SortOrder = TasksViewModel.SortOrder.NEWEST_FIRST

    class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(formattedDate: String) {
            binding.dateHeaderText.text = formattedDate
        }
    }

    class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(task: Task, onItemClick: (Task) -> Unit, onItemLongClick: (Task) -> Unit, onToggleTask: (Task) -> Unit) {
            binding.apply {
                taskText.text = task.title
                taskCheckbox.isChecked = task.isCompleted
                
                // Отображение описания задачи
                if (task.description != null && task.description.isNotBlank()) {
                    taskDescription.text = task.description
                    taskDescription.visibility = View.VISIBLE
                } else {
                    taskDescription.visibility = View.GONE
                }
                
                // Скрываем дату в самой задаче
                taskDueDate.visibility = View.GONE
                
                // Изменяем внешний вид в зависимости от статуса
                if (task.isCompleted) {
                    taskText.alpha = 0.6f
                    taskText.paintFlags = taskText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    taskDescription.alpha = 0.6f
                } else {
                    taskText.alpha = 1.0f
                    taskText.paintFlags = taskText.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    taskDescription.alpha = 1.0f
                }
                
                // Клик на задачу - детальный просмотр
                root.setOnClickListener { onItemClick(task) }
                
                // Клик на чекбокс - переключить статус выполнения
                taskCheckbox.setOnClickListener {
                    onToggleTask(task)
                }
                
                // Клик на крестик - удалить задачу
                deleteTaskButton.setOnClickListener {
                    onItemLongClick(task)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TaskListItem.DateHeader -> 0
            is TaskListItem.TaskItem -> 1
            else -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateHeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TaskListItem.DateHeader -> {
                (holder as DateHeaderViewHolder).bind(item.formattedDate)
            }
            is TaskListItem.TaskItem -> {
                (holder as TaskViewHolder).bind(item.task, onItemClick, onItemLongClick, onToggleTask)
            }
        }
    }

    fun submitTasks(tasks: List<Task>, sortOrder: TasksViewModel.SortOrder = TasksViewModel.SortOrder.NEWEST_FIRST) {
        currentSortOrder = sortOrder
        val groupedItems = groupTasksByDate(tasks, sortOrder)
        submitList(groupedItems)
    }

    private fun groupTasksByDate(tasks: List<Task>, sortOrder: TasksViewModel.SortOrder): List<TaskListItem> {
        // При любой сортировке создаем блоки дат только для последовательных задач
        return createSequentialDateGroups(tasks)
    }
    
    /**
     * Создает блоки дат только для последовательных задач с одной датой
     */
    private fun createSequentialDateGroups(tasks: List<Task>): List<TaskListItem> {
        val items = mutableListOf<TaskListItem>()
        
        if (tasks.isEmpty()) return items
        
        var currentDate = tasks[0].date
        var currentFormattedDate = formatTaskDate(currentDate)
        
        // Добавляем первый заголовок даты
        items.add(TaskListItem.DateHeader(currentDate, currentFormattedDate))
        items.add(TaskListItem.TaskItem(tasks[0]))
        
        for (i in 1 until tasks.size) {
            val task = tasks[i]
            val taskFormattedDate = formatTaskDate(task.date)
            
            // Если дата изменилась, добавляем новый заголовок
            if (task.date != currentDate) {
                currentDate = task.date
                currentFormattedDate = taskFormattedDate
                items.add(TaskListItem.DateHeader(currentDate, currentFormattedDate))
            }
            
            items.add(TaskListItem.TaskItem(task))
        }
        
        return items
    }

    private fun formatTaskDate(dateString: String): String {
        val taskDate = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            return dateString
        }
        
        if (taskDate == null) return dateString
        
        val taskCalendar = Calendar.getInstance()
        taskCalendar.time = taskDate
        
        // Получаем сегодняшнюю дату без времени
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        // Получаем вчерашнюю дату без времени
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        yesterday.set(Calendar.HOUR_OF_DAY, 0)
        yesterday.set(Calendar.MINUTE, 0)
        yesterday.set(Calendar.SECOND, 0)
        yesterday.set(Calendar.MILLISECOND, 0)
        
        // Сбрасываем время у даты задачи
        taskCalendar.set(Calendar.HOUR_OF_DAY, 0)
        taskCalendar.set(Calendar.MINUTE, 0)
        taskCalendar.set(Calendar.SECOND, 0)
        taskCalendar.set(Calendar.MILLISECOND, 0)
        
        return when {
            taskCalendar.timeInMillis == today.timeInMillis -> "Сегодня"
            taskCalendar.timeInMillis == yesterday.timeInMillis -> "Вчера"
            taskCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
                val monthFormat = SimpleDateFormat("dd MMM", Locale("ru"))
                monthFormat.format(taskDate).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
            }
            else -> {
                val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("ru"))
                fullDateFormat.format(taskDate).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
            }
        }
    }

    private fun parseDate(formattedDate: String): Long {
        // Преобразуем форматированную дату обратно в timestamp для сортировки
        val calendar = Calendar.getInstance()
        
        // Сбрасываем время на начало дня для корректной сортировки
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return when {
            formattedDate == "Сегодня" -> calendar.timeInMillis
            formattedDate == "Вчера" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }
            else -> {
                try {
                    val monthFormat = SimpleDateFormat("dd MMM", Locale("ru"))
                    val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("ru"))
                    
                    // Сбрасываем время у парсера тоже
                    monthFormat.isLenient = false
                    fullDateFormat.isLenient = false
                    
                    val date = if (formattedDate.split(" ").size == 2) {
                        monthFormat.parse(formattedDate)
                    } else {
                        fullDateFormat.parse(formattedDate)
                    }
                    date?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
    }

    private class TaskListItemDiffCallback : DiffUtil.ItemCallback<TaskListItem>() {
        override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
            return when {
                oldItem is TaskListItem.DateHeader && newItem is TaskListItem.DateHeader -> oldItem.date == newItem.date
                oldItem is TaskListItem.TaskItem && newItem is TaskListItem.TaskItem -> oldItem.task.id == newItem.task.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
            return oldItem == newItem
        }
    }
}
