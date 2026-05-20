package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.repository.AppBackupRepository
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    private val appBackupRepository: AppBackupRepository
) {
    suspend operator fun invoke(json: String, replaceExisting: Boolean): Result<Unit> =
        appBackupRepository.importAllFromJsonString(json, replaceExisting)
}
