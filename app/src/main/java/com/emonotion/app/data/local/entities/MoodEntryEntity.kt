package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.emonotion.app.domain.model.MoodType

/**
 * Entity для записи о настроении в базе данных Room
 */
@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey 
    val id: String,
    val mood: String, // enum MoodType.toString()
    val intensity: Int, // 1-5
    val emotions: String, // JSON строка для эмоций
    val activities: String, // JSON строка для активностей
    val notes: String?,
    val timestamp: Long,
    val date: String // yyyy-MM-dd
) {
    companion object {
        fun fromDomain(moodEntry: com.emonotion.app.domain.model.MoodEntry): MoodEntryEntity {
            return MoodEntryEntity(
                id = moodEntry.id,
                mood = moodEntry.mood?.name ?: "",
                intensity = moodEntry.intensity,
                emotions = moodEntry.emotions.joinToString(","),
                activities = moodEntry.activities.joinToString(","),
                notes = moodEntry.notes,
                timestamp = moodEntry.timestamp,
                date = moodEntry.date
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.MoodEntry {
        return com.emonotion.app.domain.model.MoodEntry(
            id = id,
            mood = if (mood.isEmpty()) null else MoodType.valueOf(mood),
            intensity = intensity,
            emotions = if (emotions.isEmpty()) emptyList() else emotions.split(","),
            activities = if (activities.isEmpty()) emptyList() else activities.split(","),
            notes = notes,
            timestamp = timestamp,
            date = date
        )
    }
}
