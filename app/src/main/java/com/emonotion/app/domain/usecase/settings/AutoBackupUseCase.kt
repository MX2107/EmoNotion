package com.emonotion.app.domain.usecase.settings

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emonotion.app.worker.AutoBackupWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AutoBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule(intervalHours: Long) {
        val hours = intervalHours.coerceIn(15L, 24L * 30)
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(hours, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "emonotion_auto_backup"
    }
}
