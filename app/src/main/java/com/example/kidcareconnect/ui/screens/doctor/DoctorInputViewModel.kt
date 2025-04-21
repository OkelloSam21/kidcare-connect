package com.example.kidcareconnect.ui.screens.doctor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.AuthManager
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.*
import com.example.kidcareconnect.ui.screens.child.HealthLogUi
import com.example.kidcareconnect.ui.screens.meal.MealScheduleFormData
import com.example.kidcareconnect.ui.screens.medication.MedicationFormData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Events from Doctor Input Screen
sealed class DoctorInputEvent {
    object HealthLogAdded : DoctorInputEvent()
    object MedicationAdded : DoctorInputEvent()
    object DietaryPlanAdded : DoctorInputEvent()
    object CaretakerAssigned : DoctorInputEvent()
    data class ShowMessage(val message: String) : DoctorInputEvent()
}

// UI State for Doctor Input Screen
data class DoctorInputUiState(
    val childId: String = "",
    val childName: String = "",
    val healthLogs: List<HealthLogUi> = emptyList(),
    val caretakers: List<UserEntity> = emptyList(),
    val assignedCaretakers: List<UserEntity> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// Health Log Form Data
data class HealthLogFormData(
    val temperature: Float?,
    val heartRate: Int?,
    val symptoms: String?,
    val notes: String?
)

// Caretaker Assignment Form Data
data class CaretakerAssignmentData(
    val caretakerId: String
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class DoctorInputViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val healthRepository: HealthRepository,
    private val medicationRepository: MedicationRepository,
    private val dietaryRepository: DietaryRepository,
    private val authManager: AuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorInputUiState())
    val uiState: StateFlow<DoctorInputUiState> = _uiState.asStateFlow()

    private val _events = Channel<DoctorInputEvent>()
    val events = _events.receiveAsFlow()

    init {
        // Get the childId from the navigation arguments
        savedStateHandle.get<String>("childId")?.let { childId ->
            _uiState.update { it.copy(childId = childId) }
            loadChildData(childId)
        }

        checkUserRole()
        loadCaretakers()
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            val currentUser = authManager.currentUser.value
            val isAdmin = currentUser?.role == UserRole.ADMIN

            _uiState.update { state ->
                state.copy(isAdmin = isAdmin)
            }
        }
    }

    private fun loadChildData(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load child basic info
                childRepository.getChildById(childId)?.let { child ->
                    _uiState.update { state ->
                        state.copy(childName = child.name)
                    }
                }

                // Load health logs
                healthRepository.getHealthLogsForChild(childId).collect { healthLogs ->
                    val healthLogsUi = healthLogs.map { log ->
                        HealthLogUi(
                            id = log.logId,
                            date = formatDate(log.loggedAt),
                            time = formatTime(log.loggedAt),
                            temperature = log.temperature,
                            heartRate = log.heartRate,
                            symptoms = log.symptoms,
                            notes = log.notes
                        )
                    }

                    _uiState.update { state ->
                        state.copy(
                            healthLogs = healthLogsUi,
                            isLoading = false
                        )
                    }
                }

                // Load assigned caretakers
                loadAssignedCaretakers(childId)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading doctor input data"
                    )
                }
            }
        }
    }

    private fun loadCaretakers() {
        viewModelScope.launch {
            try {
                userRepository.getUsersByRole(UserRole.CARETAKER).collect { caretakers ->
                    _uiState.update { state ->
                        state.copy(caretakers = caretakers)
                    }
                }
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage("Error loading caretakers: ${e.message}"))
            }
        }
    }

    private fun loadAssignedCaretakers(childId: String) {
        viewModelScope.launch {
            try {
                // Get all caretakers assigned to this child
                val assignedCaretakerIds = mutableListOf<String>()

                // In a real implementation, you would have a proper repository method for this
                // For now, just simulate by getting relevant data and filtering
                val caretakers = userRepository.getUsersByRole(UserRole.CARETAKER).firstOrNull() ?: emptyList()

                // Get assigned caretakers
                val assignedCaretakers = caretakers.filter { caretaker ->
                    // Check if this caretaker is assigned to the child
                    // In a real implementation, you would query the database directly
                    childRepository.getChildrenForCaretaker(caretaker.userId).firstOrNull()?.any {
                        it.childId == childId
                    } ?: false
                }

                _uiState.update { state ->
                    state.copy(assignedCaretakers = assignedCaretakers)
                }
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage("Error loading assigned caretakers: ${e.message}"))
            }
        }
    }

//    fun addHealthLog(healthLog: HealthLogFormData) {
//        viewModelScope.launch {
//            try {
//                // Only admins can add health logs through this interface
//                if (!_uiState.value.isAdmin) {
//                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can add health records"))
//                    return@launch
//                }
//
//                val childId = _uiState.value.childId
//                val currentUserId = authManager.getCurrentUserId() ?: "user1"
//
//                healthRepository.createHealthLog(
//                    childId = childId,
//                    temperature = healthLog.temperature,
//                    heartRate = healthLog.heartRate,
//                    symptoms = healthLog.symptoms,
//                    notes = healthLog.notes,
//                    loggedBy = currentUserId
//                )
//
//                // Reload health logs
//                loadChildData(childId)
//
//                _events.send(DoctorInputEvent.HealthLogAdded)
//            } catch (e: Exception) {
//                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error adding health log"))
//            }
//        }
//    }

    fun addMedication(medication: MedicationFormData) {
        viewModelScope.launch {
            try {
                // Only admins can add medications
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can add medications"))
                    return@launch
                }

                val childId = _uiState.value.childId
                val currentUserId = authManager.getCurrentUserId() ?: "user1"

                // Create the medication
                val newMedication = medicationRepository.createMedication(
                    childId = childId,
                    name = medication.name,
                    dosage = medication.dosage,
                    instructions = medication.instructions,
                    startDate = LocalDate.parse(medication.startDate),
                    endDate = medication.endDate?.let { LocalDate.parse(it) },
                    frequency = medication.frequency,
                    priority = medication.priority,
                    createdBy = currentUserId
                )

                // Create schedules for each specified time
                medication.times.forEach { time ->
                    medicationRepository.createMedicationSchedule(
                        medicationId = newMedication.medicationId,
                        time = time,
                        days = medication.days
                    )
                }

                _events.send(DoctorInputEvent.MedicationAdded)
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error adding medication"))
            }
        }
    }

    fun addDietaryPlan(
        allergies: String?,
        restrictions: String?,
        preferences: String?,
        notes: String?,
        mealSchedules: List<MealScheduleFormData>
    ) {
        viewModelScope.launch {
            try {
                // Only admins can add dietary plans
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can add dietary plans"))
                    return@launch
                }

                val childId = _uiState.value.childId
                val currentUserId = authManager.getCurrentUserId() ?: "user1"

                // Create the dietary plan
                val dietaryPlan = dietaryRepository.createDietaryPlan(
                    childId = childId,
                    allergies = allergies,
                    restrictions = restrictions,
                    preferences = preferences,
                    notes = notes,
                    createdBy = currentUserId
                )

                // Create meal schedules for the dietary plan
                mealSchedules.forEach { mealSchedule ->
                    dietaryRepository.createMealSchedule(
                        planId = dietaryPlan.planId,
                        mealType = mealSchedule.mealType,
                        time = mealSchedule.time,
                        days = mealSchedule.days.joinToString(","),
                        menu = mealSchedule.menu
                    )
                }

                _events.send(DoctorInputEvent.DietaryPlanAdded)
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error adding dietary plan"))
            }
        }
    }

    fun assignCaretaker(caretakerId: String) {
        viewModelScope.launch {
            try {
                // Only admins can assign caretakers
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can assign caretakers"))
                    return@launch
                }

                val childId = _uiState.value.childId

                // Assign caretaker to child
                childRepository.assignCaretakerToChild(caretakerId, childId)

                // Reload assigned caretakers
                loadAssignedCaretakers(childId)

                _events.send(DoctorInputEvent.CaretakerAssigned)
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error assigning caretaker"))
            }
        }
    }

    fun removeCaretakerAssignment(caretakerId: String) {
        viewModelScope.launch {
            try {
                // Only admins can remove caretaker assignments
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can remove caretaker assignments"))
                    return@launch
                }

                val childId = _uiState.value.childId

                // Remove caretaker assignment
                childRepository.removeCaretakerAssignment(caretakerId, childId)

                // Reload assigned caretakers
                loadAssignedCaretakers(childId)

                _events.send(DoctorInputEvent.ShowMessage("Caretaker assignment removed"))
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error removing caretaker assignment"))
            }
        }
    }

    fun addHealthLog(healthLog: HealthLogFormData) {
        viewModelScope.launch {
            try {
                // Only admins can add health logs through this interface
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can add health records"))
                    return@launch
                }

                val childId = _uiState.value.childId
                val currentUserId = authManager.getCurrentUserId() ?: "user1"

                healthRepository.createHealthLog(
                    childId = childId,
                    temperature = healthLog.temperature,
                    heartRate = healthLog.heartRate,
                    symptoms = healthLog.symptoms,
                    notes = healthLog.notes,
                    loggedBy = currentUserId
                )

                // Reload health logs
                loadChildData(childId)

                _events.send(DoctorInputEvent.HealthLogAdded)
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error adding health log"))
            }
        }
    }

    private fun formatDate(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        return dateTime.format(formatter)
    }

    private fun formatTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return dateTime.format(formatter)
    }
}