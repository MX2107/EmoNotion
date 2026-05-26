package com.emonotion.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import java.io.File
import com.emonotion.app.data.local.dao.CustomActivityDao
import com.emonotion.app.data.local.dao.CustomMoodDao
import com.emonotion.app.data.local.dao.CustomTagDao
import com.emonotion.app.data.local.dao.MoodDao
import com.emonotion.app.data.local.dao.NoteDao
import com.emonotion.app.data.local.dao.TaskDao
import com.emonotion.app.data.local.dao.UserDao
import com.emonotion.app.data.local.entities.MoodEntryEntity
import com.emonotion.app.data.local.entities.NoteEntity
import com.emonotion.app.data.local.entities.TaskEntity
import com.emonotion.app.data.local.entities.UserProfileEntity
import com.emonotion.app.data.repository.backup.EmoNotionBackupPayload
import com.emonotion.app.domain.repository.AppBackupRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moodDao: MoodDao,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val userDao: UserDao,
    private val customTagDao: CustomTagDao,
    private val customActivityDao: CustomActivityDao,
    private val customMoodDao: CustomMoodDao,
    private val gson: Gson
) : AppBackupRepository {

    override suspend fun exportAllToJsonString(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val profile = userDao.getUserProfile("current_user")
            android.util.Log.d("BackupExport", "Profile: name=${profile?.name}, avatar=${profile?.avatar}, bio=${profile?.bio}")
            
            // Кодируем аватар в base64 если он существует
            val avatarData = profile?.avatar?.let { avatarPath ->
                val avatarFile = File(avatarPath)
                if (avatarFile.exists()) {
                    try {
                        val bytes = avatarFile.readBytes()
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                    } catch (e: Exception) {
                        android.util.Log.e("BackupExport", "Failed to encode avatar: ${e.message}")
                        null
                    }
                } else {
                    android.util.Log.w("BackupExport", "Avatar file not found: $avatarPath")
                    null
                }
            }
            
            val payload = EmoNotionBackupPayload(
                version = 3,
                exportedAt = System.currentTimeMillis(),
                moods = moodDao.getAllMoodsList(),
                notes = noteDao.getAllNotesList(),
                tasks = taskDao.getAllTasksList(),
                profile = profile,
                customTags = customTagDao.getAllCustomTagsList().ifEmpty { null },
                customActivities = customActivityDao.getAllCustomActivitiesList().ifEmpty { null },
                customMoods = customMoodDao.getAllCustomMoodsList().ifEmpty { null },
                avatarData = avatarData
            )
            val json = gson.toJson(payload)
            android.util.Log.d("BackupExport", "JSON length: ${json.length}, avatarData included: ${avatarData != null}")
            json
        }
    }

    override suspend fun exportAllToDownloads(): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val json = exportAllToJsonString().getOrThrow()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                error("Экспорт в загрузки доступен с Android 10 (API 29)+")
            }
            val name = "emonotion_backup_${System.currentTimeMillis()}.json"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/EmoNotion/Backup"
                )
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Не удалось создать файл")
            resolver.openOutputStream(uri, "w")?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            } ?: error("Не удалось записать файл")
            uri
        }
    }

    override suspend fun exportAllToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = exportAllToJsonString().getOrThrow()
            val resolver = context.contentResolver
            resolver.openOutputStream(uri, "w")?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            } ?: error("Не удалось записать файл")
            Unit
        }
    }

    override suspend fun importAllFromJsonString(json: String, replaceExisting: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = gson.fromJson(json, EmoNotionBackupPayload::class.java)
                    ?: error("Некорректный JSON")
                if (payload.version !in listOf(1, 2, 3)) error("Неподдерживаемая версия бэкапа: ${payload.version}")
                if (replaceExisting) {
                    moodDao.deleteAllMoods()
                    noteDao.deleteAllNotes()
                    taskDao.deleteAllTasks()
                    userDao.deleteAllUserProfiles()
                    customTagDao.deleteAllCustomTags()
                    customActivityDao.deleteAllCustomActivities()
                    customMoodDao.deleteAllCustomMoods()
                }
                payload.moods?.let { if (it.isNotEmpty()) moodDao.insertMoods(it) }
                payload.notes?.let { if (it.isNotEmpty()) noteDao.insertNotes(it) }
                payload.tasks?.let { if (it.isNotEmpty()) taskDao.insertTasks(it) }
                // Импорт профиля с сохранением существующих данных если они отсутствуют в бэкапе
                payload.profile?.let { backupProfile ->
                    android.util.Log.d("BackupImport", "Backup profile: name=${backupProfile.name}, avatar=${backupProfile.avatar}, bio=${backupProfile.bio}, email=${backupProfile.email}")
                    
                    // Декодируем и сохраняем аватар из base64 (версия 3+)
                    val avatarPath = if (payload.version >= 3 && payload.avatarData != null) {
                        try {
                            val avatarBytes = Base64.decode(payload.avatarData, Base64.NO_WRAP)
                            val avatarDir = File(context.filesDir, "avatars")
                            if (!avatarDir.exists()) {
                                avatarDir.mkdirs()
                            }
                            val avatarFile = File(avatarDir, "avatar_current_user.jpg")
                            avatarFile.writeBytes(avatarBytes)
                            android.util.Log.d("BackupImport", "Avatar saved to: ${avatarFile.absolutePath}")
                            avatarFile.absolutePath
                        } catch (e: Exception) {
                            android.util.Log.e("BackupImport", "Failed to decode avatar: ${e.message}")
                            backupProfile.avatar // Fallback to original path
                        }
                    } else {
                        backupProfile.avatar
                    }
                    
                    val existingProfile = userDao.getUserProfile("current_user")
                    android.util.Log.d("BackupImport", "Existing profile: ${existingProfile != null}")
                    
                    if (existingProfile != null && !replaceExisting) {
                        // Слияние данных: сохраняем существующие avatar и bio если в бэкапе они null
                        val mergedProfile = existingProfile.copy(
                            name = backupProfile.name,
                            email = backupProfile.email,
                            avatar = avatarPath ?: existingProfile.avatar,
                            bio = backupProfile.bio,
                            updatedAt = System.currentTimeMillis()
                        )
                        android.util.Log.d("BackupImport", "Merged profile: avatar=${mergedProfile.avatar}, bio=${mergedProfile.bio}")
                        userDao.updateUserProfile(mergedProfile)
                    } else {
                        // Обновляем профиль с новым путём к аватару
                        val profileWithAvatar = backupProfile.copy(avatar = avatarPath)
                        android.util.Log.d("BackupImport", "Replacing profile: name=${profileWithAvatar.name}, avatar=${profileWithAvatar.avatar}, bio=${profileWithAvatar.bio}")
                        userDao.updateUserProfile(profileWithAvatar)
                    }
                    
                    // Проверяем, что профиль сохранился корректно
                    val savedProfile = userDao.getUserProfile("current_user")
                    android.util.Log.d("BackupImport", "Saved profile: name=${savedProfile?.name}, avatar=${savedProfile?.avatar}, bio=${savedProfile?.bio}")
                }
                // Импорт кастомных данных (версия 2+)
                if (payload.version >= 2) {
                    payload.customTags?.let { if (it.isNotEmpty()) customTagDao.insertCustomTags(it) }
                    payload.customActivities?.let { if (it.isNotEmpty()) customActivityDao.insertCustomActivities(it) }
                    payload.customMoods?.let { if (it.isNotEmpty()) customMoodDao.insertCustomMoods(it) }
                }
                Unit
            }
        }
}
