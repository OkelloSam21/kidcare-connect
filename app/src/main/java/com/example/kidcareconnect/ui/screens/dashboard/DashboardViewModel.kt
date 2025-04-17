package com.example.kidcareconnect.ui.screens.dashboard

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.AuthManager
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
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
    val showAddChildDialog: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val medicationRepository: MedicationRepository,
    private val dietaryRepository: DietaryRepository,
    private val notificationRepository: NotificationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val TAG = "DashboardViewModel"
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadCurrentUser()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading current user from AuthManager")

                // Get current user from AuthManager
                val currentUser = authManager.currentUser.value
                val currentUserRole = authManager.currentUserRole.value

                if (currentUser != null) {
                    Log.d(TAG, "Current user: ${currentUser.name} with role $currentUserRole")

                    _uiState.update { state ->
                        state.copy(
                            currentUser = currentUser,
                            currentUserRole = currentUserRole,
                            isLoading = true
                        )
                    }

                    // Now that we have the user, load children and notifications
                    loadChildren(currentUser.userId, currentUserRole)
                    loadUnreadNotificationsCount(currentUser.userId)
                } else {
                    Log.w(TAG, "No authenticated user found in AuthManager")
                    // Try to find any user as fallback
                    val allUsers = userRepository.getAllUsers().firstOrNull() ?: emptyList()
                    if (allUsers.isNotEmpty()) {
                        val user = allUsers.first()
                        Log.w(TAG, "Using fallback user: ${user.name} with role ${user.role}")
                        _uiState.update { state ->
                            state.copy(
                                currentUser = user,
                                currentUserRole = user.role,
                                isLoading = true
                            )
                        }

                        // Load data with fallback user
                        loadChildren(user.userId, user.role)
                        loadUnreadNotificationsCount(user.userId)
                    } else {
                        Log.e(TAG, "No users found in database")
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load user: ${e.message}"
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadChildren(userId: String, role: UserRole) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                Log.d(TAG, "Loading children for user $userId with role $role")

                // Get all children (for Admin) or assigned children (for Caretaker)
                when (role) {
                    UserRole.ADMIN -> {
                        childRepository.getAllChildren().collectLatest { allChildren ->
                            Log.d(TAG, "Admin: Found ${allChildren.size} children")
                            _uiState.update { state ->
                                state.copy(
                                    children = allChildren,
                                    isLoading = false
                                )
                            }

                            // Also get assigned children for filtering
                            childRepository.getChildrenForCaretaker(userId).collectLatest { assignedChildren ->
                                _uiState.update { state ->
                                    state.copy(myAssignedChildren = assignedChildren)
                                }
                            }

                            loadPendingTasks(allChildren)
                        }
                    }
                    UserRole.CARETAKER -> {
                        childRepository.getChildrenForCaretaker(userId).collectLatest { assignedChildren ->
                            Log.d(TAG, "Caretaker: Found ${assignedChildren.size} assigned children")
                            _uiState.update { state ->
                                state.copy(
                                    children = assignedChildren,
                                    myAssignedChildren = assignedChildren,
                                    isLoading = false
                                )
                            }
                            loadPendingTasks(assignedChildren)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading children", e)
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
    fun loadPendingTasks(children: List<Child>) {
        viewModelScope.launch {
            try {
                if (children.isEmpty()) {
                    Log.d(TAG, "No children to load tasks for")
                    _uiState.update { it.copy(pendingTasks = emptyList()) }
                    return@launch
                }

                Log.d(TAG, "Loading pending tasks for ${children.size} children")
                val tasks = mutableListOf<PendingTaskUi>()

                // Get pending medication tasks for each child
                children.forEach { child ->
                    try {
                        medicationRepository.getPendingMedicationsForChild(child.childId).collectLatest { logs ->
                            Log.d(TAG, "Found ${logs.size} pending medications for ${child.name}")

                            logs.forEach { log ->
                                // Get medication details
                                medicationRepository.getMedicationsForChild(child.childId).collectLatest { medications ->
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
                                                childName = child.name,
                                                title = "Give ${medication.name} to ${child.name}",
                                                description = "Dose: ${medication.dosage}",
                                                time = formatDateTime(log.scheduledTime),
                                                type = "medication",
                                                priority = priority
                                            )
                                        )
                                    }
                                }
                            }

                            // Sort tasks by priority (high to low) and then by time
                            val sortedTasks = tasks.sortedWith(
                                compareByDescending<PendingTaskUi> { it.priority }
                                    .thenBy { it.time }
                            )

                            Log.d(TAG, "Total pending tasks: ${sortedTasks.size}")

                            _uiState.update { state ->
                                state.copy(pendingTasks = sortedTasks)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading pending medications for child ${child.name}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading pending tasks", e)
                _uiState.update { state ->
                    state.copy(errorMessage = e.message ?: "Error loading tasks")
                }
            }
        }
    }

    private fun loadUnreadNotificationsCount(userId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.getUnreadNotificationsForUser(userId).collectLatest { notifications ->
                    Log.d(TAG, "Found ${notifications.size} unread notifications")
                    _uiState.update { state ->
                        state.copy(unreadNotificationsCount = notifications.size)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notifications", e)
            }
        }
    }

    fun onChildSelected(childId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Child selected: $childId")
            _events.send(DashboardEvent.NavigateToChild(childId))
        }
    }

    fun onTaskSelected(task: PendingTaskUi) {
        viewModelScope.launch {
            Log.d(TAG, "Task selected: ${task.title} for child ${task.childName}")
            _events.send(DashboardEvent.NavigateToChild(task.childId))
        }
    }

    fun toggleAddChildDialog(show: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(showAddChildDialog = show)
            }
        }
    }

    fun onAddChildClicked() {
        toggleAddChildDialog()
    }

    fun createNewChild(
        name:String,
        dateOfBirth: LocalDate,
        gender: String,
        bloodGroup: String,
        emergencyContact: String,
        note: String
    ) {
        viewModelScope.launch {
            try {

                childRepository.createChild(
                    name = name,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    emergencyContact = emergencyContact,
                    notes = note,
                )

                _events.send(DashboardEvent.ShowMessage("Child added successfully"))

                val currentUserId = _uiState.value.currentUser?.userId ?: return@launch

                val currentUserRole = _uiState.value.currentUserRole

                loadChildren(currentUserId, currentUserRole)
            } catch (e: Exception) {
                _events.send(DashboardEvent.ShowMessage("Failed to add Child: ${e.message}"))
            }
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