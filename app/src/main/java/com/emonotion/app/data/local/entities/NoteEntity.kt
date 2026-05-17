package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONException

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
    val tags: String = "", // JSON строка для List<String>
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
                tags = tagsToJson(note.tags),
                isPinned = note.isPinned
            )
        }
        
        private fun tagsToJson(tags: List<String>): String {
            if (tags.isEmpty()) return ""
            val jsonArray = JSONArray()
            tags.forEach { jsonArray.put(it) }
            return jsonArray.toString()
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.Note {
        return com.emonotion.app.domain.model.Note(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            date = date,
            tags = jsonToTags(tags),
            isPinned = isPinned
        )
    }
    
    private fun jsonToTags(jsonString: String): List<String> {
        if (jsonString.isEmpty()) return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val tags = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                tags.add(jsonArray.getString(i))
            }
            tags
        } catch (e: JSONException) {
            // Fallback для старых данных, сохраненных через запятую
            jsonString.split(",").filter { it.isNotBlank() }
        }
    }
}
