package com.emonotion.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emonotion.app.di.BackupEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                BackupEntryPoint::class.java
            )
            val json = entryPoint.appBackupRepository().exportAllToJsonString().getOrThrow()
            val dir = File(applicationContext.filesDir, "backups").apply { mkdirs() }
            val file = File(dir, "auto_${System.currentTimeMillis()}.json")
            file.writeText(json)
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("AutoBackupWorker", "backup failed", e)
            Result.retry()
        }
    }
}
