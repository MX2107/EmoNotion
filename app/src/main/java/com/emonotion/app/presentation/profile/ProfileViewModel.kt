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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    
    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()
    
    private val _editedBio = MutableStateFlow("")
    val editedBio: StateFlow<String> = _editedBio.asStateFlow()
    
    private val _avatar = MutableStateFlow("")
    val avatar: StateFlow<String> = _avatar.asStateFlow()
    
    private val _editedAvatar = MutableStateFlow("")
    val editedAvatar: StateFlow<String> = _editedAvatar.asStateFlow()
    
    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private var profileCollectJob: Job? = null
    private var statsCollectJob: Job? = null
    
    /**
     * Загружает профиль пользователя (одна подписка на Flow Room)
     */
    fun loadUserProfile() {
        if (profileCollectJob?.isActive == true) return

        profileCollectJob = viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                _userProfile.value = profile
                profile?.let {
                    _name.value = it.name
                    _email.value = it.email ?: ""
                    _bio.value = it.bio ?: ""
                    _avatar.value = it.avatar ?: ""
                    _editedName.value = it.name
                    _editedEmail.value = it.email ?: ""
                    _editedBio.value = it.bio ?: ""
                    _editedAvatar.value = it.avatar ?: ""
                }
            }
        }

        loadUserStats()
    }
    
    /**
     * Загружает статистику пользователя
     */
    private fun loadUserStats() {
        if (statsCollectJob?.isActive == true) return

        statsCollectJob = viewModelScope.launch {
            getUserStatsUseCase().collect { stats ->
                android.util.Log.d("ProfileViewModel", "loadUserStats: stats=$stats")
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
            _editedBio.value = profile.bio ?: ""
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
     * Обновляет информацию о себе
     */
    fun updateBio(bio: String) {
        _editedBio.value = bio
    }
    
    /**
     * Обновляет URL аватара
     */
    fun updateAvatar(avatar: String) {
        _editedAvatar.value = avatar
    }
    
    /**
     * Очищает аватар пользователя
     */
    fun clearAvatar() {
        _editedAvatar.value = ""
    }
    
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

        val avatar = _editedAvatar.value.ifEmpty {
            _userProfile.value?.avatar.orEmpty()
        }
        
        android.util.Log.d("ProfileViewModel", "saveProfile: name='$name', email='${_editedEmail.value}', bio='${_editedBio.value}'")
        val profile = _userProfile.value
        android.util.Log.d("ProfileViewModel", "saveProfile: currentProfile=$profile")
        
        if (profile == null) {
            createProfile(name, _editedEmail.value, _editedBio.value, avatar)
        } else {
            updateProfile(profile, name, _editedEmail.value, _editedBio.value, avatar)
        }
    }
    
    private fun createProfile(name: String, email: String, bio: String, avatar: String) {
        executeWithLoading {
            viewModelScope.launch {
                val profile = UserProfile(
                    userId = "current_user",
                    name = name,
                    email = email,
                    bio = bio,
                    avatar = avatar,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                android.util.Log.d("ProfileViewModel", "createProfile: profile=$profile")
                try {
                    val result = updateUserProfileUseCase(profile)
                    android.util.Log.d("ProfileViewModel", "createProfile: result=$result")
                    if (result.isSuccess) {
                        _userProfile.value = profile
                        _isEditing.value = false
                        _successMessage.value = "Профиль успешно создан"
                    } else {
                        _errorMessage.value = "Ошибка создания профиля"
                        result.exceptionOrNull()?.let { 
                            android.util.Log.e("ProfileViewModel", "createProfile error", it)
                        }
                    }
                } catch (error: Exception) {
                    android.util.Log.e("ProfileViewModel", "createProfile exception", error)
                    _errorMessage.value = "Ошибка создания профиля: ${error.message}"
                }
            }
        }
    }
    
    private fun updateProfile(profile: UserProfile, name: String, email: String, bio: String, avatar: String) {
        executeWithLoading {
            viewModelScope.launch {
                val updatedProfile = profile.copy(
                    name = name,
                    email = email,
                    bio = bio,
                    avatar = avatar,
                    updatedAt = System.currentTimeMillis()
                )
                android.util.Log.d("ProfileViewModel", "updateProfile: updatedProfile=$updatedProfile")
                try {
                    val result = updateUserProfileUseCase(updatedProfile)
                    android.util.Log.d("ProfileViewModel", "updateProfile: result=$result")
                    if (result.isSuccess) {
                        _userProfile.value = updatedProfile
                        // Обновляем локальные состояния
                        _name.value = updatedProfile.name
                        _email.value = updatedProfile.email ?: ""
                        _bio.value = updatedProfile.bio ?: ""
                        _avatar.value = updatedProfile.avatar ?: ""
                        _editedName.value = updatedProfile.name
                        _editedEmail.value = updatedProfile.email ?: ""
                        _editedBio.value = updatedProfile.bio ?: ""
                        _editedAvatar.value = updatedProfile.avatar ?: ""
                        _isEditing.value = false
                        _successMessage.value = "Профиль успешно обновлен"
                    } else {
                        _errorMessage.value = "Ошибка обновления профиля"
                        result.exceptionOrNull()?.let { 
                            android.util.Log.e("ProfileViewModel", "updateProfile error", it)
                        }
                    }
                } catch (error: Exception) {
                    android.util.Log.e("ProfileViewModel", "updateProfile exception", error)
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
                        bio = "",
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
               _editedBio.value != (profile.bio ?: "") ||
               _editedAvatar.value != (profile.avatar ?: "")
    }
    
    /**
     * Проверяет валидность данных профиля
     */
    fun isProfileValid(): Boolean {
        return _editedName.value.trim().isNotEmpty()
    }
}
