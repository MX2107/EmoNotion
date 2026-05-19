package com.emonotion.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.emonotion.app.domain.repository.AvatarRepository as DomainAvatarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : DomainAvatarRepository {
    private val avatarsDir: File
        get() = File(context.filesDir, "avatars").apply {
            if (!exists()) mkdirs()
        }

    override suspend fun saveAvatar(bitmap: Bitmap, userId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Удаляем старый аватар если есть
            deleteAvatar(userId)

            // Создаем файл для нового аватара
            val avatarFile = File(avatarsDir, "avatar_$userId.jpg")
            
            // Сжимаем и сохраняем
            FileOutputStream(avatarFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            Result.success(avatarFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadAvatar(path: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                Result.success(bitmap)
            } else {
                Result.failure(Exception("Failed to decode bitmap"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAvatar(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val avatarFile = File(avatarsDir, "avatar_$userId.jpg")
            if (avatarFile.exists()) {
                avatarFile.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAvatarByPath(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvatarPath(userId: String): String {
        return File(avatarsDir, "avatar_$userId.jpg").absolutePath
    }

    override fun avatarExists(userId: String): Boolean {
        return File(avatarsDir, "avatar_$userId.jpg").exists()
    }
}
