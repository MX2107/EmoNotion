package com.emonotion.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application класс для инициализации Hilt
 */
@HiltAndroidApp
class EmoNotionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Применяем тему глобально при запуске приложения
        com.emonotion.app.utils.ThemeManager.applyFromStorage(this)
    }
}
