package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.repository.GmailSyncRepository
import javax.inject.Inject

class EnableAutoSyncUseCase @Inject constructor(
    private val gmailSyncRepository: GmailSyncRepository
) {
    suspend operator fun invoke(enabled: Boolean, frequencyOrdinal: Int): Result<Unit> =
        gmailSyncRepository.setAutoSyncEnabled(enabled, frequencyOrdinal)
}
