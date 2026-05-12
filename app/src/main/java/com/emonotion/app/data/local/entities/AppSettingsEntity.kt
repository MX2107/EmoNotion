package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.emonotion.app.domain.model.ThemeMode

/**
 * Entity для настроек приложения в базе данных Room
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey 
    val key: String = "main_settings",
    val theme: String, // enum ThemeMode.toString()
    val notificationsEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = false,
    val language: String = "ru",
    val reminderTime: String = "09:00", // HH:mm
    val moodReminderEnabled: Boolean = true,
    val updatedAt: Long
) {
    companion object {
        fun fromDomain(appSettings: com.emonotion.app.domain.model.AppSettings): AppSettingsEntity {
            return AppSettingsEntity(
                theme = appSettings.theme.name,
                notificationsEnabled = appSettings.notificationsEnabled,
                autoBackupEnabled = appSettings.autoBackupEnabled,
                language = appSettings.language,
                reminderTime = appSettings.reminderTime,
                moodReminderEnabled = appSettings.moodReminderEnabled,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.AppSettings {
        return com.emonotion.app.domain.model.AppSettings(
            theme = ThemeMode.valueOf(theme),
            notificationsEnabled = notificationsEnabled,
            autoBackupEnabled = autoBackupEnabled,
            language = language,
            reminderTime = reminderTime,
            moodReminderEnabled = moodReminderEnabled
        )
    }
}
