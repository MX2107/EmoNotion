package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.emonotion.app.domain.model.TaskPriority

/**
 * Entity для задачи в базе данных Room
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey 
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val timestamp: Long,
    val date: String, // yyyy-MM-dd
    val priority: String // enum TaskPriority.toString()
) {
    companion object {
        fun fromDomain(task: com.emonotion.app.domain.model.Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                isCompleted = task.isCompleted,
                timestamp = task.timestamp,
                date = task.date,
                priority = task.priority.name
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.Task {
        return com.emonotion.app.domain.model.Task(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            timestamp = timestamp,
            date = date,
            priority = TaskPriority.valueOf(priority)
        )
    }
}
