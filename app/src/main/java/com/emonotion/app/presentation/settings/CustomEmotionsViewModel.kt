package com.emonotion.app.presentation.settings

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.CustomMood
import com.emonotion.app.domain.usecase.custommood.AddCustomMoodUseCase
import com.emonotion.app.domain.usecase.custommood.DeleteCustomMoodUseCase
import com.emonotion.app.domain.usecase.custommood.GetCustomMoodsUseCase
import com.emonotion.app.domain.usecase.custommood.UpdateCustomMoodUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана редактирования пользовательских эмоций
 */
@HiltViewModel
class CustomEmotionsViewModel @Inject constructor(
    private val addCustomMoodUseCase: AddCustomMoodUseCase,
    private val deleteCustomMoodUseCase: DeleteCustomMoodUseCase,
    private val getCustomMoodsUseCase: GetCustomMoodsUseCase,
    private val updateCustomMoodUseCase: UpdateCustomMoodUseCase
) : BaseViewModel() {

    private val _customMoods = MutableStateFlow<List<CustomMood>>(emptyList())
    val customMoods: StateFlow<List<CustomMood>> = _customMoods.asStateFlow()

    init {
        loadCustomMoods()
    }

    private fun loadCustomMoods() {
        viewModelScope.launch {
            try {
                getCustomMoodsUseCase.getAllCustomMoods().collect { moods ->
                    _customMoods.value = moods
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки эмоций: ${e.message}"
            }
        }
    }

    fun addCustomEmotion(name: String) {
        executeWithResult(
            operation = {
                val customMood = CustomMood(
                    id = UUID.randomUUID().toString(),
                    name = name.trim()
                )
                addCustomMoodUseCase(customMood)
            },
            onSuccess = {
                _successMessage.value = "Эмоция '$name' добавлена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при добавлении эмоции: $message"
            }
        )
    }

    fun deleteCustomMood(customMood: CustomMood) {
        executeWithResult(
            operation = {
                deleteCustomMoodUseCase(customMood)
            },
            onSuccess = {
                _successMessage.value = "Эмоция '${customMood.name}' удалена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при удалении эмоции: $message"
            }
        )
    }

    fun updateCustomEmotion(customMood: CustomMood) {
        executeWithResult(
            operation = {
                updateCustomMoodUseCase(customMood)
            },
            onSuccess = {
                _successMessage.value = "Эмоция '${customMood.name}' обновлена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при обновлении эмоции: $message"
            }
        )
    }
}
