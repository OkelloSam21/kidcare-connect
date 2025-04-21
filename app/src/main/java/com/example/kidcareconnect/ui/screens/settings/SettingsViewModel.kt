package com.example.kidcareconnect.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.AuthManager
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.repository.ThemeRepository
import com.example.kidcareconnect.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

// UI State for Settings
data class SettingsUiState(
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userProfilePictureUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val theme: String = "system",
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false,
    val isProfileEditing: Boolean = false
)

// Form data for profile update
data class ProfileUpdateData(
    val name: String,
    val email: String,
    val phone: String
)

sealed class SettingsEvent {
    data object NavigateToLogin : SettingsEvent()
    data class ShowMessage(val message: String) : SettingsEvent()
    data object ProfileUpdated : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val themeRepository: ThemeRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _event = Channel<SettingsEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadUserSettings()
        viewModelScope.launch {
            themeRepository.themeFlow.collect { theme ->
                _uiState.update { it.copy(theme = theme) }
            }
        }
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Get current user from AuthManager
                val currentUser = authManager.currentUser.value

                if (currentUser != null) {
                    _uiState.update { state ->
                        state.copy(
                            userId = currentUser.userId,
                            userName = currentUser.name,
                            userEmail = currentUser.email,
                            userPhone = currentUser.phone,
                            userProfilePictureUrl = currentUser.profilePictureUrl,
                            isAdmin = currentUser.role.name == "ADMIN",
                            isLoading = false
                        )
                    }
                } else {
                    // Fallback to fetching the mocked user
                    val mockUserId = "user1"
                    val user = userRepository.getUserById(mockUserId)
                    user?.let {
                        _uiState.update { state ->
                            state.copy(
                                userId = it.userId,
                                userName = it.name,
                                userEmail = it.email,
                                userPhone = it.phone,
                                userProfilePictureUrl = it.profilePictureUrl,
                                isAdmin = it.role.name == "ADMIN",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.send(SettingsEvent.ShowMessage("Error loading user settings: ${e.message}"))
            }
        }
    }

    fun toggleProfileEditing(editing: Boolean) {
        _uiState.update { it.copy(isProfileEditing = editing) }
    }

    fun updateProfile(profileData: ProfileUpdateData) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Get the current user entity
                val userId = _uiState.value.userId
                val user = userRepository.getUserById(userId)

                if (user != null) {
                    // Update user with new data
                    val updatedUser = user.copy(
                        name = profileData.name,
                        email = profileData.email,
                        phone = profileData.phone,
                        updatedAt = LocalDateTime.now()
                    )

                    // Save to repository
                    userRepository.updateUser(updatedUser)

                    // Update in AuthManager
                    authManager.setCurrentUser(updatedUser)

                    // Update UI state
                    _uiState.update { state ->
                        state.copy(
                            userName = profileData.name,
                            userEmail = profileData.email,
                            userPhone = profileData.phone,
                            isProfileEditing = false,
                            isLoading = false
                        )
                    }

                    _event.send(SettingsEvent.ProfileUpdated)
                    _event.send(SettingsEvent.ShowMessage("Profile updated successfully"))
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.send(SettingsEvent.ShowMessage("User not found"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.send(SettingsEvent.ShowMessage("Error updating profile: ${e.message}"))
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(theme = theme) }
        viewModelScope.launch {
            themeRepository.saveThemeSetting(theme)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId
                userRepository.clearUserSession(userId)
                authManager.clearCurrentUser()
                _event.send(SettingsEvent.NavigateToLogin)
            } catch (e: Exception) {
                _event.send(SettingsEvent.ShowMessage("Error signing out: ${e.message}"))
            }
        }
    }
}