package com.emonotion.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemTaskBinding
import com.emonotion.app.domain.model.Task
import java.text.SimpleDateFormat
import java.util.*

/**
 * Адаптер для списка задач
 */
class TasksAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onItemLongClick: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        
        fun bind(task: Task, onItemClick: (Task) -> Unit, onItemLongClick: (Task) -> Unit) {
            binding.apply {
                taskText.text = task.title
                taskCheckbox.isChecked = task.isCompleted
                
                // Изменяем внешний вид в зависимости от статуса
                if (task.isCompleted) {
                    taskText.alpha = 0.6f
                } else {
                    taskText.alpha = 1.0f
                }
                
                root.setOnClickListener { onItemClick(task) }
                taskCheckbox.setOnClickListener { 
                    onItemClick(task)
                }
                deleteTaskButton.setOnClickListener { 
                    onItemLongClick(task)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onItemLongClick)
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
