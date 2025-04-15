package com.example.kidcareconnect.presentation.ui.screen.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import com.example.kidcareconnect.presentation.data.local.entity.HealthCheck
import com.example.kidcareconnect.presentation.data.repository.ChildRepository
import com.example.kidcareconnect.presentation.data.repository.HealthCheckRepository
import com.example.kidcareconnect.presentation.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// UI State for Health Screen
data class HealthScreenUiState(
    val childId: String = "",
    val childName: String = "",
    val healthChecks: List<HealthCheck> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hasVitalData: Boolean = false,
    val temperature: Float? = null,
    val lastDiaper: Long? = null,
    val scrollState: ScalingLazyListState = ScalingLazyListState()
)

// Events for navigation and UI interactions
sealed class HealthScreenEvent {
    object NavigateBack : HealthScreenEvent()
    object ShowTemperatureInput : HealthScreenEvent()
    object ShowDiaperInput : HealthScreenEvent()
    data class ShowAddNoteDialog(val checkId: String) : HealthScreenEvent()
}

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val healthCheckRepository: HealthCheckRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthScreenUiState())
    val uiState: StateFlow<HealthScreenUiState> = _uiState.asStateFlow()

    private val _events = Channel<HealthScreenEvent>()
    val events = _events.receiveAsFlow()

    // Initialize with child ID
    fun initialize(childId: String) {
        _uiState.update { it.copy(childId = childId, isLoading = true) }
        loadChildInfo(childId)
        loadHealthChecks(childId)
        loadVitalSigns(childId)
    }

    private fun loadChildInfo(childId: String) {
        viewModelScope.launch {
            try {
                val child = childRepository.getChild(childId).firstOrNull()
                if (child != null) {
                    _uiState.update { it.copy(childName = child.name) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Child not found") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Failed to load child information",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadHealthChecks(childId: String) {
        viewModelScope.launch {
            try {
                healthCheckRepository.getHealthChecksForChild(childId).collect { healthChecks ->
                    _uiState.update {
                        it.copy(
                            healthChecks = healthChecks,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // If we're in a development environment or testing, load preview data
                if (isInDevelopmentMode()) {
                    _uiState.update {
                        it.copy(
                            healthChecks = HealthCheckRepository.getPreviewData(childId),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to load health checks",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadVitalSigns(childId: String) {
        viewModelScope.launch {
            try {
                // In a real implementation, we would fetch this from a database
                // For now, we'll just use mock data for demonstration
                if (isInDevelopmentMode()) {
                    _uiState.update {
                        it.copy(
                            hasVitalData = true,
                            temperature = 98.6f, // Normal temperature
                            lastDiaper = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // 2 hours ago
                        )
                    }
                }
            } catch (e: Exception) {
                // Just ignore errors in loading vital signs, as they're optional
            }
        }
    }

    fun markHealthCheckCompleted(checkId: String) {
        viewModelScope.launch {
            try {
                healthCheckRepository.logHealthCheckCompleted(checkId)

                // Also complete any related tasks
                completeRelatedTasks(checkId)

                // Refresh health checks list
                loadHealthChecks(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update health check status")
                }
            }
        }
    }

    fun onAddNote(checkId: String) {
        viewModelScope.launch {
            _events.send(HealthScreenEvent.ShowAddNoteDialog(checkId))
        }
    }

    fun onLogTemperature() {
        viewModelScope.launch {
            _events.send(HealthScreenEvent.ShowTemperatureInput)
        }
    }

    fun onLogDiaper() {
        viewModelScope.launch {
            _events.send(HealthScreenEvent.ShowDiaperInput)

            // In a real implementation, we would show a dialog and save the data
            // For demonstration, we'll just update the UI state
            _uiState.update {
                it.copy(
                    lastDiaper = System.currentTimeMillis()
                )
            }
        }
    }

    fun onLogNap() {
        viewModelScope.launch {
            // In a real implementation, we would show a dialog to log nap details
            // For now, we'll just create a new health check
            try {
                val newCheck = HealthCheck(
                    id = UUID.randomUUID().toString(),
                    childId = _uiState.value.childId,
                    checkType = "nap",
                    scheduledTime = System.currentTimeMillis(),
                    notes = "Nap started at ${formatTime(System.currentTimeMillis())}",
                    lastChecked = System.currentTimeMillis()
                )

                healthCheckRepository.addHealthCheck(newCheck)

                // Refresh health checks list
                loadHealthChecks(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to log nap")
                }
            }
        }
    }

    // This would be called from a dialog after the user enters a temperature
    fun saveTemperature(temperature: Float) {
        viewModelScope.launch {
            try {
                // In a real implementation, we would save this to a database
                // For now, we'll just update the UI state
                _uiState.update {
                    it.copy(
                        temperature = temperature,
                        hasVitalData = true
                    )
                }

                // Create a new health check if the temperature is abnormal
                if (temperature > 100.4f) {
                    val newCheck = HealthCheck(
                        id = UUID.randomUUID().toString(),
                        childId = _uiState.value.childId,
                        checkType = "temperature",
                        scheduledTime = System.currentTimeMillis() + 60 * 60 * 1000, // 1 hour from now
                        notes = "Follow-up for high temperature of ${temperature}°F",
                        lastChecked = System.currentTimeMillis()
                    )

                    healthCheckRepository.addHealthCheck(newCheck)

                    // Refresh health checks list
                    loadHealthChecks(_uiState.value.childId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to save temperature")
                }
            }
        }
    }

    private suspend fun completeRelatedTasks(checkId: String) {
        // Find and complete any tasks related to this health check
        val childId = _uiState.value.childId
        val pendingTasks = taskRepository.getPendingTasksForChild(childId).firstOrNull() ?: return

        for (task in pendingTasks) {
            if (task.type == "health" && task.title.contains(checkId)) {
                taskRepository.completeTask(task.id)
            }
        }
    }

    // Utility function to check if we're in development mode
    private fun isInDevelopmentMode(): Boolean {
        return true // For simplicity, always return true during development
    }

    // Utility function for time formatting
    private fun formatTime(timeMillis: Long): String {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(timeMillis))
    }
}