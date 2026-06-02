package com.emonotion.app.presentation.settings

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.usecase.customtag.AddCustomTagUseCase
import com.emonotion.app.domain.usecase.customtag.DeleteCustomTagUseCase
import com.emonotion.app.domain.usecase.customtag.GetCustomTagsUseCase
import com.emonotion.app.domain.usecase.customtag.UpdateCustomTagUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана редактирования пользовательских тегов
 */
@HiltViewModel
class CustomTagsViewModel @Inject constructor(
    private val addCustomTagUseCase: AddCustomTagUseCase,
    private val deleteCustomTagUseCase: DeleteCustomTagUseCase,
    private val getCustomTagsUseCase: GetCustomTagsUseCase,
    private val updateCustomTagUseCase: UpdateCustomTagUseCase
) : BaseViewModel() {

    private val _customTags = MutableStateFlow<List<CustomTag>>(emptyList())
    val customTags: StateFlow<List<CustomTag>> = _customTags.asStateFlow()

    init {
        loadCustomTags()
    }

    private fun loadCustomTags() {
        viewModelScope.launch {
            try {
                getCustomTagsUseCase.getAllCustomTags().collect { tags ->
                    _customTags.value = tags
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки тегов: ${e.message}"
            }
        }
    }

    fun addCustomTag(name: String) {
        executeWithResult(
            operation = {
                val customTag = CustomTag(
                    id = UUID.randomUUID().toString(),
                    name = name.trim()
                )
                addCustomTagUseCase(customTag)
            },
            onSuccess = {
                _successMessage.value = "Тег '$name' добавлен"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при добавлении тега: $message"
            }
        )
    }

    fun deleteCustomTag(customTag: CustomTag) {
        executeWithResult(
            operation = {
                deleteCustomTagUseCase(customTag)
            },
            onSuccess = {
                _successMessage.value = "Тег '${customTag.name}' удален"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при удалении тега: $message"
            }
        )
    }

    fun updateCustomTag(customTag: CustomTag) {
        executeWithResult(
            operation = {
                updateCustomTagUseCase(customTag)
            },
            onSuccess = {
                _successMessage.value = "Тег '${customTag.name}' обновлен"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при обновлении тега: $message"
            }
        )
    }
}
