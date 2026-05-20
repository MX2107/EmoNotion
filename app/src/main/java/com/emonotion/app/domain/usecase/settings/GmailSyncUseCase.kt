package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.repository.GmailSyncRepository
import javax.inject.Inject

class GmailSyncUseCase @Inject constructor(
    private val gmailSyncRepository: GmailSyncRepository
) {
    suspend fun syncNow(): Result<Unit> = gmailSyncRepository.syncNow()
}
