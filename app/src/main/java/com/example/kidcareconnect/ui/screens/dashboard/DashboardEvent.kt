package com.example.kidcareconnect.ui.screens.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.ChildRepository
import com.example.kidcareconnect.data.repository.DietaryRepository
import com.example.kidcareconnect.data.repository.MedicationRepository
import com.example.kidcareconnect.data.repository.NotificationRepository
import com.example.kidcareconnect.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Events from Dashboard
sealed class DashboardEvent {
    data class NavigateToChild(val childId: String) : DashboardEvent()
    data class ShowMessage(val message: String) : DashboardEvent()
}

// Pending Task UI model
data class PendingTaskUi(
    val id: String,
    val childId: String,
    val childName: String,
    val title: String,
    val description: String,
    val time: String,
    val type: String, // medication, meal, health
    val priority: Int // 0: normal, 1: high, 2: critical
)

// UI State for Dashboard
data class DashboardUiState(
    val currentUser: UserEntity? = null,
    val currentUserRole: UserRole = UserRole.CARETAKER,
    val children: List<Child> = emptyList(),
    val myAssignedChildren: List<Child> = emptyList(),
    val pendingTasks: List<PendingTaskUi> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val medicationRepository: MedicationRepository,
    private val dietaryRepository: DietaryRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()
    
    // Mock current user for development
    private val mockUserId = "user1"
    
    init {
        loadCurrentUser()
        loadUnreadNotificationsCount()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            // In a real app, this would come from auth system
            val user = userRepository.getUserById(mockUserId)
            user?.let {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentUser = it,
                        currentUserRole = it.role
                    )
                }
            }
        }
    }
    
    fun loadChildren() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Get all children (for Admin) or assigned children (for Caretaker)
                when (_uiState.value.currentUserRole) {
                    UserRole.ADMIN -> {
                        childRepository.getAllChildren().collect { allChildren ->
                            _uiState.update { state ->
                                state.copy(
                                    children = allChildren,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    UserRole.CARETAKER -> {
                        childRepository.getChildrenForCaretaker(mockUserId).collect { assignedChildren ->
                            _uiState.update { state ->
                                state.copy(
                                    children = assignedChildren,
                                    myAssignedChildren = assignedChildren,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading children"
                    )
                }
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPendingTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val tasks = mutableListOf<PendingTaskUi>()
            val childMap = mutableMapOf<String, String>() // childId -> childName
            
            // Get all children names for reference
            try {
                childRepository.getAllChildren().collect { allChildren ->
                    allChildren.forEach { child ->
                        childMap[child.childId] = child.name
                    }
                }
                
                // Get pending medication tasks for each child
                _uiState.value.children.forEach { child ->
                    medicationRepository.getPendingMedicationsForChild(child.childId).collect { logs ->
                        logs.forEach { log ->
                            // Get medication details
                            medicationRepository.getMedicationsForChild(child.childId).collect { medications ->
                                val medication = medications.find { it.medicationId == log.medicationId }
                                medication?.let {
                                    val priority = when (medication.priority) {
                                        MedicationPriority.CRITICAL -> 2
                                        MedicationPriority.HIGH -> 1
                                        else -> 0
                                    }
                                    
                                    tasks.add(
                                        PendingTaskUi(
                                            id = log.logId,
                                            childId = child.childId,
                                            childName = childMap[child.childId] ?: "Unknown",
                                            title = "Give ${medication.name} to ${childMap[child.childId]}",
                                            description = "Dose: ${medication.dosage}",
                                            time = formatDateTime(log.scheduledTime),
                                            type = "medication",
                                            priority = priority
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // TODO: Add meal tasks here in similar fashion
                }
                
                // Sort tasks by priority (high to low) and then by time
                val sortedTasks = tasks.sortedWith(
                    compareByDescending<PendingTaskUi> { it.priority }
                        .thenBy { it.time }
                )
                
                _uiState.update { state ->
                    state.copy(
                        pendingTasks = sortedTasks,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading tasks"
                    )
                }
            }
        }
    }
    
    private fun loadUnreadNotificationsCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadNotificationsForUser(mockUserId).collect { notifications ->
                _uiState.update { state ->
                    state.copy(unreadNotificationsCount = notifications.size)
                }
            }
        }
    }
    
    fun onChildSelected(childId: String) {
        viewModelScope.launch {
            _events.send(DashboardEvent.NavigateToChild(childId))
        }
    }
    
    fun onTaskSelected(task: PendingTaskUi) {
        viewModelScope.launch {
            _events.send(DashboardEvent.NavigateToChild(task.childId))
        }
    }
    
    fun onAddChildClicked() {
        // In a real app, this would navigate to an add child form
        viewModelScope.launch {
            _events.send(DashboardEvent.ShowMessage("Add child functionality to be implemented"))
        }
    }
    
    fun childHasPendingTasks(childId: String): Boolean {
        return _uiState.value.pendingTasks.any { it.childId == childId }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateAge(dateOfBirth: LocalDate): String {
        val now = LocalDate.now()
        val period = Period.between(dateOfBirth, now)
        return when {
            period.years > 0 -> "${period.years} yr"
            period.months > 0 -> "${period.months} mo"
            else -> "${period.days} days"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return dateTime.format(formatter)
    }
}