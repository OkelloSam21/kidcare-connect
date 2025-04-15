// ThemeRepository.kt
package com.example.kidcareconnect.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_setting")
    
    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[themeKey] ?: "system"
        }
    
    suspend fun saveThemeSetting(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme
        }
    }
}