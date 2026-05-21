package com.emonotion.app.data.repository.backup

import com.emonotion.app.data.local.entities.CustomActivityEntity
import com.emonotion.app.data.local.entities.CustomMoodEntity
import com.emonotion.app.data.local.entities.CustomTagEntity
import com.emonotion.app.data.local.entities.MoodEntryEntity
import com.emonotion.app.data.local.entities.NoteEntity
import com.emonotion.app.data.local.entities.TaskEntity
import com.emonotion.app.data.local.entities.UserProfileEntity

/**
 * Полный снимок локальных данных для экспорта/импорта JSON.
 */
data class EmoNotionBackupPayload(
    val version: Int = 3,
    val exportedAt: Long = System.currentTimeMillis(),
    val moods: List<MoodEntryEntity>? = null,
    val notes: List<NoteEntity>? = null,
    val tasks: List<TaskEntity>? = null,
    val profile: UserProfileEntity? = null,
    val customTags: List<CustomTagEntity>? = null,
    val customActivities: List<CustomActivityEntity>? = null,
    val customMoods: List<CustomMoodEntity>? = null,
    val avatarData: String? = null // Base64 encoded avatar image
)
