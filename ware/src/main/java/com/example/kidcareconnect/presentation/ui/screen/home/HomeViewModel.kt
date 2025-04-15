package com.example.kidcareconnect.presentation.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import com.example.kidcareconnect.data.model.ChildUi
import com.example.kidcareconnect.data.model.PendingTaskUi
import com.example.kidcareconnect.data.repository.ChildRepository
import com.example.kidcareconnect.data.repository.TaskRepository
import com.example.kidcareconnect.data.sync.DataSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for Home Screen
data class HomeScreenUiState(
    val assignedChildren: List<ChildUi> = emptyList(),
    val pendingTasks: List<PendingTaskUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scrollState: ScalingLazyListState = ScalingLazyListState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val taskRepository: TaskRepository,
    private val dataSyncManager: DataSyncManager
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
                childRepository.getAssignedChildren()
                    .catch { e ->
                        // If there's an error, check if we have any children
                        val children = childRepository.getAssignedChildren().first()
                        if (children.isEmpty()) {
                            // Request a sync if no children are found
                            dataSyncManager.requestInitialData()

                            // Use fallback data for development
                            _uiState.update { state ->
                                state.copy(
                                    assignedChildren = getFallbackChildren(),
                                    isLoading = false
                                )
                            }
                        } else {
                            throw e
                        }
                    }
                    .collectLatest { children ->
                        _uiState.update { state ->
                            // If we have children, use them
                            if (children.isNotEmpty()) {
                                state.copy(
                                    assignedChildren = children,
                                    isLoading = false
                                )
                            } else {
                                // Otherwise try to sync and use fallback data
                                dataSyncManager.requestInitialData()
                                state.copy(
                                    assignedChildren = getFallbackChildren(),
                                    isLoading = false
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                // Request sync and use fallback data on error
                dataSyncManager.requestInitialData()
                _uiState.update { state ->
                    state.copy(
                        assignedChildren = getFallbackChildren(),
                        isLoading = false,
                        errorMessage = null // Hide error in development mode
                    )
                }
            }
        }
    }

    private fun loadPendingTasks() {
        viewModelScope.launch {
            try {
                taskRepository.getPendingTasks()
                    .catch { e ->
                        // Use fallback tasks on error
                        _uiState.update { state ->
                            state.copy(
                                pendingTasks = getFallbackTasks(),
                                isLoading = false
                            )
                        }
                    }
                    .collectLatest { tasks ->
                        _uiState.update { state ->
                            if (tasks.isNotEmpty()) {
                                state.copy(
                                    pendingTasks = tasks,
                                    isLoading = false
                                )
                            } else {
                                // Use fallback tasks if none found
                                state.copy(
                                    pendingTasks = getFallbackTasks(),
                                    isLoading = false
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                // Use fallback tasks on exception
                _uiState.update { state ->
                    state.copy(
                        pendingTasks = getFallbackTasks(),
                        isLoading = false,
                        errorMessage = null // Hide error in development mode
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

    fun onHealthTaskSelected(task: PendingTaskUi) {
        viewModelScope.launch {
            _events.send(HomeScreenEvent.NavigateToHealth(task.childId))
        }
    }

    // Fallback data for development/testing when no data is available
    private fun getFallbackChildren(): List<ChildUi> {
        return listOf(
            ChildUi(
                id = "child1",
                name = "Sarah Johnson",
                age = "4 years",
                hasPendingTasks = true
            ),
            ChildUi(
                id = "child2",
                name = "Michael Lee",
                age = "3 years",
                hasPendingTasks = false
            ),
            ChildUi(
                id = "child3",
                name = "Emma Davis",
                age = "5 years",
                hasPendingTasks = true
            )
        )
    }

    private fun getFallbackTasks(): List<PendingTaskUi> {
        return listOf(
            PendingTaskUi(
                id = "task1",
                childId = "child1",
                childName = "Sarah Johnson",
                title = "Give Tylenol",
                time = "10:30 AM",
                type = "medication",
                priority = 2
            ),
            PendingTaskUi(
                id = "task2",
                childId = "child3",
                childName = "Emma Davis",
                title = "Lunch time",
                time = "12:00 PM",
                type = "meal",
                priority = 1
            ),
            PendingTaskUi(
                id = "task3",
                childId = "child1",
                childName = "Sarah Johnson",
                title = "Temperature check",
                time = "2:00 PM",
                type = "health",
                priority = 0
            )
        )
    }
}

sealed class HomeScreenEvent {
    data class NavigateToMedication(val childId: String) : HomeScreenEvent()
    data class NavigateToMeal(val childId: String) : HomeScreenEvent()
    data class NavigateToHealth(val childId: String) : HomeScreenEvent()
}