package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.repository.DataLocationRepository
import javax.inject.Inject

class ChangeDataLocationUseCase @Inject constructor(
    private val dataLocationRepository: DataLocationRepository
) {
    operator fun invoke(treeUri: String?) {
        dataLocationRepository.setPreferredStorageUri(treeUri)
    }
}
