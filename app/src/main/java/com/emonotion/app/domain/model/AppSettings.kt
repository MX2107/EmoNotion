package com.emonotion.app.domain.model

/**
 * Модель настроек приложения
 */
data class AppSettings(
    val theme: ThemeMode = ThemeMode.LIGHT,
    val notificationsEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = false,
    val language: String = "ru",
    val reminderTime: String = "09:00", // HH:mm
    val moodReminderEnabled: Boolean = true
)

/**
 * Режимы темы
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
