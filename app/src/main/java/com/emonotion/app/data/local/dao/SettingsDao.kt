package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с настройками приложения
 */
@Dao
interface SettingsDao {
    
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingsEntity>>
    
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String = "main_settings"): AppSettingsEntity?
    
    @Query("SELECT * FROM app_settings WHERE key = :key")
    fun getSettingFlow(key: String = "main_settings"): Flow<AppSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSetting(setting: AppSettingsEntity)
    
    @Update
    suspend fun updateSettings(setting: AppSettingsEntity)
    
    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSetting(key: String = "main_settings")
    
    @Query("DELETE FROM app_settings")
    suspend fun deleteAllSettings()
}
