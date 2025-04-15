package com.example.kidcareconnect.presentation.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.presentation.data.model.ChildUi
import com.example.kidcareconnect.presentation.data.model.PendingTaskUi
import com.example.kidcareconnect.wearos.data.repository.ChildRepository
import com.example.kidcareconnect.wearos.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for Home Screen
data class HomeScreenUiState(
    val assignedChildren: List<ChildUi> = emptyList(),
    val pendingTasks: List<PendingTaskUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeScreenEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadAssignedChildren()
        loadPendingTasks()
    }

    private fun loadAssignedChildren() {
        viewModelScope.launch {
            try {
                childRepository.getAssignedChildren().collect { children ->
                    _uiState.update { state ->
                        state.copy(
                            assignedChildren = children,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load children"
                    )
                }
            }
        }
    }

    private fun loadPendingTasks() {
        viewModelScope.launch {
            try {
                taskRepository.getPendingTasks().collect { tasks ->
                    _uiState.update { state ->
                        state.copy(
                            pendingTasks = tasks,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load tasks"
                    )
                }
            }
        }
    }

    fun onMedicationSelected(childId: String) {
        viewModelScope.launch {
            _events.send(HomeScreenEvent.NavigateToMedication(childId))
        }
    }

    fun onMealSelected(childId: String) {
        viewModelScope.launch {
            _events.send(HomeScreenEvent.NavigateToMeal(childId))
        }
    }

    fun onHealthSelected(childId: String) {
        viewModelScope.launch {
            _events.send(HomeScreenEvent.NavigateToHealth(childId))
        }
    }

    fun onMedicationTaskSelected(task: PendingTaskUi) {
        viewModelScope.launch {
            _events.send(HomeScreenEvent.NavigateToMedication(task.childId))
        }
    }

    fun onMealTaskSelected(task: PendingTaskUi) {
        viewModelScope.launch {
            _events.send(HomeScreenEvent.NavigateToMeal(task.childId))
        }
    }
}

sealed class HomeScreenEvent {
    data class NavigateToMedication(val childId: String) : HomeScreenEvent()
    data class NavigateToMeal(val childId: String) : HomeScreenEvent()
    data class NavigateToHealth(val childId: String) : HomeScreenEvent()
}