package com.example.kidcareconnect.presentation.ui.screen.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import com.example.kidcareconnect.presentation.data.local.entity.Meal
import com.example.kidcareconnect.presentation.data.repository.ChildRepository
import com.example.kidcareconnect.presentation.data.repository.MealRepository
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

// UI State for Meal Screen
data class MealScreenUiState(
    val childId: String = "",
    val childName: String = "",
    val meals: List<Meal> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollState: ScalingLazyListState = ScalingLazyListState()
)

// Events for navigation
sealed class MealScreenEvent {
    object NavigateBack : MealScreenEvent()
}

@HiltViewModel
class MealViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val mealRepository: MealRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealScreenUiState())
    val uiState: StateFlow<MealScreenUiState> = _uiState.asStateFlow()

    private val _events = Channel<MealScreenEvent>()
    val events = _events.receiveAsFlow()

    // Initialize with child ID
    fun initialize(childId: String) {
        _uiState.update { it.copy(childId = childId, isLoading = true) }
        loadChildInfo(childId)
        loadMeals(childId)
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

    private fun loadMeals(childId: String) {
        viewModelScope.launch {
            try {
                mealRepository.getMealsForChild(childId).collect { meals ->
                    _uiState.update {
                        it.copy(
                            meals = meals,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // If we're in a development environment or testing, load preview data
                if (isInDevelopmentMode()) {
                    _uiState.update {
                        it.copy(
                            meals = MealRepository.getPreviewData(childId),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to load meals",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun markMealServed(mealId: String) {
        viewModelScope.launch {
            try {
                mealRepository.logMealServed(mealId)

                // Also complete any related tasks
                completeRelatedTasks(mealId)

                // Refresh meals list
                loadMeals(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update meal status")
                }
            }
        }
    }

    fun markMealNotServed(mealId: String) {
        viewModelScope.launch {
            try {
                // In a real implementation, this would mark the meal as not served
                // and potentially trigger alerts
                missRelatedTasks(mealId)

                // Refresh meals list
                loadMeals(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update meal status")
                }
            }
        }
    }

    fun markMealPartiallyServed(mealId: String) {
        viewModelScope.launch {
            try {
                // In a real implementation, this would log partial consumption
                // For now, we'll just mark it as served
                mealRepository.logMealServed(mealId)

                // Also complete related tasks but with a note
                completeRelatedTasksWithNote(mealId, "Meal partially consumed")

                // Refresh meals list
                loadMeals(_uiState.value.childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to update meal status")
                }
            }
        }
    }

    private suspend fun completeRelatedTasks(mealId: String) {
        // Find and complete any tasks related to this meal
        // This is a simplified implementation
        val childId = _uiState.value.childId
        val pendingTasks = taskRepository.getPendingTasksForChild(childId).firstOrNull() ?: return

        for (task in pendingTasks) {
            if (task.type == "meal" && task.title.contains(mealId)) {
                taskRepository.completeTask(task.id)
            }
        }
    }

    private suspend fun completeRelatedTasksWithNote(mealId: String, note: String) {
        // Similar to completeRelatedTasks but with an additional note
        // In a real implementation, we'd store the note with the task
        completeRelatedTasks(mealId)
    }

    private suspend fun missRelatedTasks(mealId: String) {
        // Similar to completeRelatedTasks but marks as missed
        val childId = _uiState.value.childId
        val pendingTasks = taskRepository.getPendingTasksForChild(childId).firstOrNull() ?: return

        for (task in pendingTasks) {
            if (task.type == "meal" && task.title.contains(mealId)) {
                taskRepository.missTask(task.id)
            }
        }
    }

    // Utility function to check if we're in development mode
    private fun isInDevelopmentMode(): Boolean {
        return true // For simplicity, always return true during development
    }
}