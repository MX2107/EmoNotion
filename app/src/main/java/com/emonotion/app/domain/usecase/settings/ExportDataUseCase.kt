package com.emonotion.app.domain.usecase.settings

import android.net.Uri
import com.emonotion.app.domain.repository.AppBackupRepository
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val appBackupRepository: AppBackupRepository
) {
    suspend operator fun invoke(): Result<Uri> = appBackupRepository.exportAllToDownloads()
}
