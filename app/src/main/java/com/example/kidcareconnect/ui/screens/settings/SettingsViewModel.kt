package com.example.kidcareconnect.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.AuthManager
import com.example.kidcareconnect.data.repository.ThemeRepository
import com.example.kidcareconnect.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for Settings
data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val theme: String = "system",
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false
)

sealed class SettingsEvent {
    data object NavigateToLogin : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val themeRepository: ThemeRepository,
    private val authManager: AuthManager
) : ViewModel()
{
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _event = Channel<SettingsEvent> ()
    val event = _event.receiveAsFlow()
    
    // Mock current user for development
    private val mockUserId = "user1"
    
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
                // Load user profile
                val user = userRepository.getUserById(mockUserId)
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            userName = it.name,
                            userEmail = it.email,
                            isAdmin = it.role.name == "ADMIN",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
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
            val userId = authManager.currentUser.value?.userId ?:""
            userRepository.clearUserSession(userId)
            authManager.clearCurrentUser()
        }
    }
}
