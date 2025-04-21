
package com.example.kidcareconnect.ui.screens.login

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.AuthManager
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for Login Screen
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val userRole: UserRole? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val TAG = "LoginViewModel"
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        // Check if already logged in
        viewModelScope.launch {
            if (authManager.isLoggedIn.value) {
                val user = authManager.currentUser.value
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            userId = user.userId,
                            userRole = user.role
                        )
                    }
                }
            } else {
                // Try to restore user from stored ID
                val wasRestored = authManager.checkForStoredUser { userId ->
                    userRepository.getUserById(userId)
                }

                if (wasRestored) {
                    val user = authManager.currentUser.value
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                userId = user.userId,
                                userRole = user.role
                            )
                        }
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // In a real app, we would verify credentials against a server
                // For demo purposes, we'll just check if the user exists in our local database
                val allUsers = userRepository.getAllUsers().firstOrNull() ?: emptyList()
                Log.d(TAG, "All users: ${allUsers.map { it.email }}")

                val foundUser = allUsers.find { it.email == email }
                Log.d(TAG, "Found user: ${foundUser?.email}")

                if (foundUser != null) {
                    // For demo purposes, we accept any password
                    // Set the user in AuthManager
                    authManager.setCurrentUser(foundUser)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userId = foundUser.userId,
                            userRole = foundUser.role
                        )
                    }
                    Log.d(TAG, "Login successful for ${foundUser.name} with role ${foundUser.role}")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid email or password"
                        )
                    }
                    Log.d(TAG, "Login failed: user not found for email $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed"
                    )
                }
            }
        }
    }

    // For development/testing - automatically log in without UI
    fun autoLogin(email: String) {
        viewModelScope.launch {
            try {
                val allUsers = userRepository.getAllUsers().firstOrNull() ?: emptyList()
                val foundUser = allUsers.find { it.email == email }

                if (foundUser != null) {
                    // Set the user in AuthManager
                    authManager.setCurrentUser(foundUser)

                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            userId = foundUser.userId,
                            userRole = foundUser.role
                        )
                    }
                    Log.d(TAG, "Auto login successful for ${foundUser.name} with role ${foundUser.role}")
                } else {
                    Log.e(TAG, "Auto login failed: no user found with email $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auto login error", e)
            }
        }
    }
}