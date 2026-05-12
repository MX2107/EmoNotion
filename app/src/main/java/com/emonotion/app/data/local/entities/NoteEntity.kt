package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для заметки в базе данных Room
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey 
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val date: String, // yyyy-MM-dd
    val tags: String = "", // JSON строка вместо List<String>
    val isPinned: Boolean = false
) {
    companion object {
        fun fromDomain(note: com.emonotion.app.domain.model.Note): NoteEntity {
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                timestamp = note.timestamp,
                date = note.date,
                tags = note.tags.joinToString(","),
                isPinned = note.isPinned
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.Note {
        return com.emonotion.app.domain.model.Note(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            date = date,
            tags = if (tags.isEmpty()) emptyList() else tags.split(","),
            isPinned = isPinned
        )
    }
}
