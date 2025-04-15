package com.example.kidcareconnect.presentation.ui.screen.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import com.example.kidcareconnect.presentation.data.local.entity.Medication
import com.example.kidcareconnect.presentation.data.repository.ChildRepository
import com.example.kidcareconnect.presentation.data.repository.MedicationRepository
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
import javax.inject.Inject

// UI State for Medication Screen
data class MedicationScreenUiState(
    val childId: String = "",
    val childName: String = "",
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollState: ScalingLazyListState = ScalingLazyListState()
)

// Events for navigation
sealed class MedicationScreenEvent {
    object NavigateBack : MedicationScreenEvent()
}

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val medicationRepository: MedicationRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationScreenUiState())
    val uiState: StateFlow<MedicationScreenUiState> = _uiState.asStateFlow()

    private val _events = Channel<MedicationScreenEvent>()
    val events = _events.receiveAsFlow()

    // Initialize with child ID
    fun initialize(childId: String) {
        _uiState.update { it.copy(childId = childId, isLoading = true) }
        loadChildInfo(childId)
        loadMedications(childId)
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

    private fun loadMedications(childId: String) {
        viewModelScope.launch {
            try {
                medicationRepository.getMedicationsForChild(childId).collect { medications ->
                    _uiState.update {
                        it.copy(
                            medications = medications,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // If we're in a development environment or testing, load preview data
                if (isInDevelopmentMode()) {
                    _uiState.update {
                        it.copy(
                            medications = MedicationRepository.getPreviewData(childId),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to load medications",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun markMedicationAdministered(medicationId: String) {
        viewModelScope.launch {
            try {
                medicationRepository.logMedicationAdministered(medicationId)

                // Also complete any related tasks
                completeRelatedTasks(medicationId)

                // Refresh medications list
                loadMedications(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update medication status")
                }
            }
        }
    }

    fun markMedicationMissed(medicationId: String) {
        viewModelScope.launch {
            try {
                // This would involve marking related tasks as missed and potentially
                // triggering alerts in a real implementation
                missRelatedTasks(medicationId)

                // Refresh medications list
                loadMedications(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update medication status")
                }
            }
        }
    }

    fun rescheduleMedication(medicationId: String) {
        viewModelScope.launch {
            try {
                // In a real implementation, this would show a time picker
                // For now, we'll just reschedule 30 minutes later
                rescheduleRelatedTasks(medicationId, System.currentTimeMillis() + 30 * 60 * 1000)

                // Refresh medications list
                loadMedications(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to reschedule medication")
                }
            }
        }
    }

    private suspend fun completeRelatedTasks(medicationId: String) {
        // Find and complete any tasks related to this medication
        // This is a simplified implementation
        val childId = _uiState.value.childId
        val pendingTasks = taskRepository.getPendingTasksForChild(childId).firstOrNull() ?: return

        for (task in pendingTasks) {
            if (task.type == "medication" && task.title.contains(medicationId)) {
                taskRepository.completeTask(task.id)
            }
        }
    }

    private suspend fun missRelatedTasks(medicationId: String) {
        // Similar to completeRelatedTasks but marks as missed
        val childId = _uiState.value.childId
        val pendingTasks = taskRepository.getPendingTasksForChild(childId).firstOrNull() ?: return

        for (task in pendingTasks) {
            if (task.type == "medication" && task.title.contains(medicationId)) {
                taskRepository.missTask(task.id)
            }
        }
    }

    private suspend fun rescheduleRelatedTasks(medicationId: String, newTime: Long) {
        // Reschedule related tasks
        val childId = _uiState.value.childId
        val pendingTasks = taskRepository.getPendingTasksForChild(childId).firstOrNull() ?: return

        for (task in pendingTasks) {
            if (task.type == "medication" && task.title.contains(medicationId)) {
                taskRepository.rescheduleTask(task.id, newTime)
            }
        }
    }

    // Utility function to check if we're in development mode
    private fun isInDevelopmentMode(): Boolean {
        return true // For simplicity, always return true during development
    }
}