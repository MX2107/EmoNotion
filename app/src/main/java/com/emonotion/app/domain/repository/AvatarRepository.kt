package com.emonotion.app.domain.repository

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с аватарами пользователей
 */
interface AvatarRepository {
    
    /**
     * Сохранить аватар пользователя
     * @param bitmap Изображение аватара
     * @param userId ID пользователя
     * @return Result с путем к сохраненному файлу или ошибкой
     */
    suspend fun saveAvatar(bitmap: Bitmap, userId: String): Result<String>
    
    /**
     * Загрузить аватар по пути
     * @param path Путь к файлу аватара
     * @return Result с Bitmap или ошибкой
     */
    suspend fun loadAvatar(path: String): Result<Bitmap>
    
    /**
     * Удалить аватар пользователя по ID
     * @param userId ID пользователя
     * @return Result успеха или ошибки
     */
    suspend fun deleteAvatar(userId: String): Result<Unit>
    
    /**
     * Удалить аватар по пути к файлу
     * @param path Путь к файлу аватара
     * @return Result успеха или ошибки
     */
    suspend fun deleteAvatarByPath(path: String): Result<Unit>
    
    /**
     * Получить путь к аватару пользователя
     * @param userId ID пользователя
     * @return Путь к файлу аватара
     */
    fun getAvatarPath(userId: String): String
    
    /**
     * Проверить существование аватара
     * @param userId ID пользователя
     * @return true если аватар существует, иначе false
     */
    fun avatarExists(userId: String): Boolean
}
