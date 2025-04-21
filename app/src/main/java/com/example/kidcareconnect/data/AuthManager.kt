
package com.example.kidcareconnect.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension for DataStore
private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

/**
 * Manages authentication state across the app
 */
@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AuthManager"

    // Keys for DataStore
    private val AUTH_USER_ID_KEY = stringPreferencesKey("auth_user_id")

    // Current authenticated user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Current user role
    private val _currentUserRole = MutableStateFlow<UserRole>(UserRole.CARETAKER)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    // Whether the user is logged in
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Flow of stored user ID from DataStore
    val storedUserIdFlow: Flow<String?> = context.authDataStore.data
        .map { preferences ->
            preferences[AUTH_USER_ID_KEY]
        }

    /**
     * Set the current authenticated user
     */
    fun setCurrentUser(user: UserEntity?) {
        Log.d(TAG, "Setting current user: ${user?.name} with role ${user?.role} userId ${user?.userId}")
        _currentUser.value = user
        user?.let {
            _currentUserRole.value = it.role
            _isLoggedIn.value = true
            // Store user ID in DataStore for persistence
            storeUserId(it.userId)
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
        // Clear stored user ID
        clearStoredUserId()
    }

    /**
     * Get the current user ID
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.userId
    }

    /**
     * Store user ID in DataStore for persistence
     */
    private fun storeUserId(userId: String) {
        try {
            Log.d(TAG, "Storing user ID: $userId")
            kotlinx.coroutines.runBlocking {
                context.authDataStore.edit { preferences ->
                    preferences[AUTH_USER_ID_KEY] = userId
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error storing user ID", e)
        }
    }

    /**
     * Clear stored user ID from DataStore
     */
    private fun clearStoredUserId() {
        try {
            Log.d(TAG, "Clearing stored user ID")
            kotlinx.coroutines.runBlocking {
                context.authDataStore.edit { preferences ->
                    preferences.remove(AUTH_USER_ID_KEY)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing stored user ID", e)
        }
    }

    /**
     * Check if there's a stored user ID and set the user if found
     * @return true if user was restored, false otherwise
     */
    suspend fun checkForStoredUser(userFinder: suspend (String) -> UserEntity?): Boolean {
        try {
            val storedUserId = storedUserIdFlow.firstOrNull()
            if (!storedUserId.isNullOrBlank()) {
                Log.d(TAG, "Found stored user ID: $storedUserId")
                val user = userFinder(storedUserId)
                if (user != null) {
                    Log.d(TAG, "Restored user: ${user.name}")
                    setCurrentUser(user)
                    return true
                } else {
                    Log.d(TAG, "Could not find user with ID: $storedUserId")
                    clearStoredUserId()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for stored user", e)
        }
        return false
    }
}