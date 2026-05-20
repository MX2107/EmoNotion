package com.emonotion.app.data.repository.backup

import com.emonotion.app.data.local.entities.MoodEntryEntity
import com.emonotion.app.data.local.entities.NoteEntity
import com.emonotion.app.data.local.entities.TaskEntity
import com.emonotion.app.data.local.entities.UserProfileEntity

/**
 * Полный снимок локальных данных для экспорта/импорта JSON.
 */
data class EmoNotionBackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val moods: List<MoodEntryEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val profile: UserProfileEntity? = null
)
