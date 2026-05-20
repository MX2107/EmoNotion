package com.emonotion.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
    private val gson: Gson
) : AppBackupRepository {

    override suspend fun exportAllToJsonString(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = EmoNotionBackupPayload(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                moods = moodDao.getAllMoodsList(),
                notes = noteDao.getAllNotesList(),
                tasks = taskDao.getAllTasksList(),
                profile = userDao.getUserProfile("current_user")
            )
            gson.toJson(payload)
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

    override suspend fun importAllFromJsonString(json: String, replaceExisting: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = gson.fromJson(json, EmoNotionBackupPayload::class.java)
                    ?: error("Некорректный JSON")
                if (payload.version != 1) error("Неподдерживаемая версия бэкапа: ${payload.version}")
                if (replaceExisting) {
                    moodDao.deleteAllMoods()
                    noteDao.deleteAllNotes()
                    taskDao.deleteAllTasks()
                    userDao.deleteAllUserProfiles()
                }
                if (payload.moods.isNotEmpty()) {
                    moodDao.insertMoods(payload.moods)
                }
                if (payload.notes.isNotEmpty()) {
                    noteDao.insertNotes(payload.notes)
                }
                if (payload.tasks.isNotEmpty()) {
                    taskDao.insertTasks(payload.tasks)
                }
                payload.profile?.let { userDao.updateUserProfile(it) }
                Unit
            }
        }
}
