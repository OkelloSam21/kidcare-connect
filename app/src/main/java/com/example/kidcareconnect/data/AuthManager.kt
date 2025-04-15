package com.example.kidcareconnect.data

import android.util.Log
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages authentication state across the app
 */
@Singleton
class AuthManager @Inject constructor() {
    private val TAG = "AuthManager"
    
    // Current authenticated user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()
    
    // Current user role
    private val _currentUserRole = MutableStateFlow<UserRole>(UserRole.CARETAKER)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()
    
    // Whether the user is logged in
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    /**
     * Set the current authenticated user
     */
    fun setCurrentUser(user: UserEntity?) {
        Log.d(TAG, "Setting current user: ${user?.name} with role ${user?.role} userId ${user?.userId}")
        _currentUser.value = user
        user?.let {
            _currentUserRole.value = it.role
            _isLoggedIn.value = true
        } ?: run {
            _isLoggedIn.value = false
        }
    }
    
    /**
     * Clear the current authenticated user (logout)
     */
    fun clearCurrentUser() {
        Log.d(TAG, "Clearing current user")
        _currentUser.value = null
        _currentUserRole.value = UserRole.CARETAKER // Default role
        _isLoggedIn.value = false
    }
    
    /**
     * Get the current user ID
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.userId
    }
}