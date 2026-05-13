package com.emonotion.app.presentation.profile

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.UserProfile
import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.domain.usecase.analytics.GetUserStatsUseCase
import com.emonotion.app.domain.usecase.profile.GetUserProfileUseCase
import com.emonotion.app.domain.usecase.profile.UpdateUserProfileUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана профиля пользователя
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase
) : BaseViewModel() {
    
    // Состояния UI
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()
    
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    
    private val _editedName = MutableStateFlow("")
    val editedName: StateFlow<String> = _editedName.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _editedEmail = MutableStateFlow("")
    val editedEmail: StateFlow<String> = _editedEmail.asStateFlow()
    
    private val _avatar = MutableStateFlow("")
    val avatar: StateFlow<String> = _avatar.asStateFlow()
    
    private val _editedAvatar = MutableStateFlow("")
    val editedAvatar: StateFlow<String> = _editedAvatar.asStateFlow()
    
    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()
    
    /**
     * Загружает профиль пользователя
     */
    fun loadUserProfile() {
        executeWithLoading {
            viewModelScope.launch {
                getUserProfileUseCase().collect { profile ->
                    _userProfile.value = profile
                    profile?.let {
                        _name.value = it.name
                        _email.value = it.email ?: ""
                        _avatar.value = it.avatar ?: ""
                        _editedName.value = it.name
                        _editedEmail.value = it.email ?: ""
                        _editedAvatar.value = it.avatar ?: ""
                    }
                }
            }
        }
        
        // Загружаем статистику пользователя
        loadUserStats()
    }
    
    /**
     * Загружает статистику пользователя
     */
    private fun loadUserStats() {
        viewModelScope.launch {
            getUserStatsUseCase().collect { stats ->
                _userStats.value = stats
            }
        }
    }
    
    /**
     * Начинает редактирование профиля
     */
    fun startEditing() {
        _isEditing.value = true
    }
    
    /**
     * Отменяет редактирование
     */
    fun cancelEditing() {
        _isEditing.value = false
        val profile = _userProfile.value
        if (profile != null) {
            _editedName.value = profile.name
            _editedEmail.value = profile.email ?: ""
            _editedAvatar.value = profile.avatar ?: ""
        }
    }
    
    /**
     * Обновляет имя пользователя
     */
    fun updateName(name: String) {
        _editedName.value = name
    }
    
    /**
     * Обновляет электронную почту пользователя
     */
    fun updateEmail(email: String) {
        _editedEmail.value = email
    }
    
    /**
     * Обновляет URL аватара
     */
    fun updateAvatar(avatar: String) {
        _editedAvatar.value = avatar
    }
    
    /**
     * Устанавливает биографию пользователя
     */
    // TODO: Добавить функциональность для bio если понадобится
    
    /**
     * Устанавливает URL аватара
     */
    fun setAvatarUrl(avatarUrl: String) {
        _editedAvatar.value = avatarUrl.trim()
    }
    
    /**
     * Сохраняет профиль пользователя
     */
    fun saveProfile() {
        val name = _editedName.value.trim()
        if (name.isEmpty()) {
            _errorMessage.value = "Имя не может быть пустым"
            return
        }
        
        val profile = _userProfile.value
        if (profile == null) {
            createProfile(name, _editedEmail.value, _editedAvatar.value)
        } else {
            updateProfile(profile, name, _editedEmail.value, _editedAvatar.value)
        }
    }
    
    private fun createProfile(name: String, email: String, avatar: String) {
        executeWithLoading {
            viewModelScope.launch {
                val profile = UserProfile(
                    userId = UUID.randomUUID().toString(),
                    name = name,
                    email = email,
                    avatar = avatar,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                try {
                    updateUserProfileUseCase(profile)
                    _userProfile.value = profile
                    _isEditing.value = false
                } catch (error: Exception) {
                    _errorMessage.value = "Ошибка создания профиля: ${error.message}"
                }
            }
        }
    }
    
    private fun updateProfile(profile: UserProfile, name: String, email: String, avatar: String) {
        executeWithLoading {
            viewModelScope.launch {
                val updatedProfile = profile.copy(
                    name = name,
                    email = email,
                    avatar = avatar,
                    updatedAt = System.currentTimeMillis()
                )
                try {
                    updateUserProfileUseCase(updatedProfile)
                    _userProfile.value = updatedProfile
                    _isEditing.value = false
                } catch (error: Exception) {
                    _errorMessage.value = "Ошибка обновления профиля: ${error.message}"
                }
            }
        }
    }
    
    /**
     * Создает новый профиль, если его нет
     */
    fun createDefaultProfile() {
        executeWithLoading {
            viewModelScope.launch {
                try {
                    val defaultProfile = UserProfile(
                        userId = "current_user",
                        name = "Пользователь",
                        email = "",
                        avatar = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    updateUserProfileUseCase(defaultProfile)
                    _userProfile.value = defaultProfile
                } catch (error: Exception) {
                    _errorMessage.value = "Ошибка создания профиля: ${error.message}"
                }
            }
        }
    }
    
    /**
     * Проверяет, есть ли изменения в профиле
     */
    fun hasChanges(): Boolean {
        val profile = _userProfile.value ?: return true
        
        return _editedName.value != profile.name ||
               _editedEmail.value != (profile.email ?: "") ||
               _editedAvatar.value != (profile.avatar ?: "")
    }
    
    /**
     * Проверяет валидность данных профиля
     */
    fun isProfileValid(): Boolean {
        return _editedName.value.trim().isNotEmpty()
    }
}
