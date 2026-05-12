package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с настройками приложения
 */
interface SettingsRepository {
    
    /**
     * Получить настройки приложения
     */
    suspend fun getSettings(): AppSettings?
    
    /**
     * Получить настройки приложения как Flow
     */
    suspend fun getSettingsFlow(): Flow<AppSettings?>
    
    /**
     * Обновить настройки приложения
     */
    suspend fun updateSettings(settings: AppSettings)
    
    /**
     * Удалить настройки
     */
    suspend fun deleteSettings()
}
