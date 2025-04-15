package com.example.kidcareconnect.ui.screens.doctor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.ChildRepository
import com.example.kidcareconnect.data.repository.HealthRepository
import com.example.kidcareconnect.data.repository.MedicationRepository
import com.example.kidcareconnect.data.repository.UserRepository
import com.example.kidcareconnect.ui.screens.child.HealthLogUi
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
    data class ShowMessage(val message: String) : DoctorInputEvent()
}

// UI State for Doctor Input Screen
data class DoctorInputUiState(
    val childId: String = "",
    val childName: String = "",
    val healthLogs: List<HealthLogUi> = emptyList(),
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

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class DoctorInputViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val healthRepository: HealthRepository,
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()
{
    
    private val _uiState = MutableStateFlow(DoctorInputUiState())
    val uiState: StateFlow<DoctorInputUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<DoctorInputEvent>()
    val events = _events.receiveAsFlow()
    
    // Mock current user for development
    private val mockUserId = "user1"
    
    init {
        // Get the childId from the navigation arguments
        savedStateHandle.get<String>("childId")?.let { childId ->
            _uiState.update { it.copy(childId = childId) }
            loadChildData(childId)
        }
        
        checkUserRole()
    }
    
    private fun checkUserRole() {
        viewModelScope.launch {
            val user = userRepository.getUserById(mockUserId)
            user?.let {
                _uiState.update { state ->
                    state.copy(isAdmin = it.role == UserRole.ADMIN)
                }
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
    
    fun addHealthLog(healthLog: HealthLogFormData) {
        viewModelScope.launch {
            try {
                // Only admins can add health logs through this interface
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can add health records"))
                    return@launch
                }
                
                val childId = _uiState.value.childId
                healthRepository.createHealthLog(
                    childId = childId,
                    temperature = healthLog.temperature,
                    heartRate = healthLog.heartRate,
                    symptoms = healthLog.symptoms,
                    notes = healthLog.notes,
                    loggedBy = mockUserId
                )
                
                // Reload health logs
                loadChildData(childId)
                
                _events.send(DoctorInputEvent.HealthLogAdded)
            } catch (e: Exception) {
                _events.send(DoctorInputEvent.ShowMessage(e.message ?: "Error adding health log"))
            }
        }
    }
    
    fun addMedication(medication: MedicationFormData) {
        viewModelScope.launch {
            try {
                // Only admins can add medications
                if (!_uiState.value.isAdmin) {
                    _events.send(DoctorInputEvent.ShowMessage("Only administrators can add medications"))
                    return@launch
                }
                
                val childId = _uiState.value.childId
                
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
                    createdBy = mockUserId
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
    
    private fun formatDate(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        return dateTime.format(formatter)
    }
    
    private fun formatTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return dateTime.format(formatter)
    }
}

