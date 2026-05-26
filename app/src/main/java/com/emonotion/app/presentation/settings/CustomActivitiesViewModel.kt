package com.emonotion.app.presentation.settings

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.CustomActivity
import com.emonotion.app.domain.usecase.customactivity.AddCustomActivityUseCase
import com.emonotion.app.domain.usecase.customactivity.DeleteCustomActivityUseCase
import com.emonotion.app.domain.usecase.customactivity.GetCustomActivitiesUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана редактирования пользовательских активностей
 */
@HiltViewModel
class CustomActivitiesViewModel @Inject constructor(
    private val addCustomActivityUseCase: AddCustomActivityUseCase,
    private val deleteCustomActivityUseCase: DeleteCustomActivityUseCase,
    private val getCustomActivitiesUseCase: GetCustomActivitiesUseCase
) : BaseViewModel() {

    private val _customActivities = MutableStateFlow<List<CustomActivity>>(emptyList())
    val customActivities: StateFlow<List<CustomActivity>> = _customActivities.asStateFlow()

    init {
        loadCustomActivities()
    }

    private fun loadCustomActivities() {
        viewModelScope.launch {
            try {
                getCustomActivitiesUseCase.getAllCustomActivities().collect { activities ->
                    _customActivities.value = activities
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки активностей: ${e.message}"
            }
        }
    }

    fun addCustomActivity(name: String) {
        executeWithResult(
            operation = {
                val customActivity = CustomActivity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim()
                )
                addCustomActivityUseCase(customActivity)
            },
            onSuccess = {
                _successMessage.value = "Активность '$name' добавлена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при добавлении активности: $message"
            }
        )
    }

    fun deleteCustomActivity(customActivity: CustomActivity) {
        executeWithResult(
            operation = {
                deleteCustomActivityUseCase(customActivity)
            },
            onSuccess = {
                _successMessage.value = "Активность '${customActivity.name}' удалена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при удалении активности: $message"
            }
        )
    }
}
