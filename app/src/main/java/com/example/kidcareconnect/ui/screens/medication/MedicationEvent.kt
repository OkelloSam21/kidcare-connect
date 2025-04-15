package com.example.kidcareconnect.ui.screens.medication

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.TaskStatus
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.MedicationRepository
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
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

// Events from Medication Screen
sealed class MedicationEvent {
    object MedicationAdded : MedicationEvent()
    object MedicationUpdated : MedicationEvent()
    object MedicationLogged : MedicationEvent()
    data class ShowMessage(val message: String) : MedicationEvent()
}

// UI State for Medication Screen
data class MedicationUiState(
    val childId: String = "",
    val medications: List<MedicationUi> = emptyList(),
    val filteredMedications: List<MedicationUi> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// UI model for Medication
data class MedicationUi(
    val id: String,
    val name: String,
    val dosage: String,
    val schedule: String,
    val instructions: String?,
    val priority: String,
    val startDate: String,
    val endDate: String?
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class MedicationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<MedicationEvent>()
    val events = _events.receiveAsFlow()
    
    // Mock current user for development
    private val mockUserId = "user1"
    
    init {
        // Get the childId from the navigation arguments
        savedStateHandle.get<String>("childId")?.let { childId ->
            _uiState.update { it.copy(childId = childId) }
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
    
    fun loadMedicationsForChild(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                medicationRepository.getMedicationsForChild(childId).collect { medications ->
                    val medicationUiList = mutableListOf<MedicationUi>()
                    
                    medications.forEach { medication ->
                        // Get schedules for each medication to display
                        val schedules = mutableListOf<String>()
                        medicationRepository.getSchedulesForMedication(medication.medicationId).collect { scheduleList ->
                            scheduleList.forEach { schedule ->
                                schedules.add("${getDaysText(schedule.days)} at ${schedule.time}")
                            }
                        }
                        
                        medicationUiList.add(
                            MedicationUi(
                                id = medication.medicationId,
                                name = medication.name,
                                dosage = medication.dosage,
                                schedule = schedules.joinToString(", "),
                                instructions = medication.instructions,
                                priority = medication.priority.name,
                                startDate = medication.startDate.toString(),
                                endDate = medication.endDate?.toString()
                            )
                        )
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            medications = medicationUiList,
                            filteredMedications = medicationUiList,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading medications"
                    )
                }
            }
        }
    }
    
    fun filterMedications(priority: MedicationPriority?) {
        val filteredList = if (priority == null) {
            _uiState.value.medications
        } else {
            _uiState.value.medications.filter { it.priority == priority.name }
        }
        
        _uiState.update { state ->
            state.copy(filteredMedications = filteredList)
        }
    }
    

    fun addMedication(
        childId: String,
        name: String,
        dosage: String,
        instructions: String?,
        startDate: String,
        endDate: String?,
        frequency: String,
        priority: MedicationPriority,
        times: List<String>,
        days: String
    ) {
        viewModelScope.launch {
            try {
                // Create the medication
                val medication = medicationRepository.createMedication(
                    childId = childId,
                    name = name,
                    dosage = dosage,
                    instructions = instructions,
                    startDate = LocalDate.parse(startDate),
                    endDate = endDate?.let { LocalDate.parse(it) },
                    frequency = frequency,
                    priority = priority,
                    createdBy = mockUserId
                )
                
                // Create schedules for each specified time
                times.forEach { time ->
                    medicationRepository.createMedicationSchedule(
                        medicationId = medication.medicationId,
                        time = time,
                        days = days
                    )
                }
                
                // Create medication logs for upcoming days
                createMedicationLogs(medication.medicationId, times, days)
                
                // Reload medications
                loadMedicationsForChild(childId)
                
                // Send event
                _events.send(MedicationEvent.MedicationAdded)
                
            } catch (e: Exception) {
                _events.send(MedicationEvent.ShowMessage(e.message ?: "Error adding medication"))
            }
        }
    }


    private fun createMedicationLogs(
        medicationId: String,
        times: List<String>,
        days: String
    ) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val daysOfWeek = days.split(",").map { it.toInt() }
                
                // Create logs for the next 7 days
                for (i in 0..6) {
                    val date = today.plusDays(i.toLong())
                    val dayOfWeek = date.dayOfWeek.value
                    
                    // Check if this day is included in the schedule
                    if (daysOfWeek.contains(dayOfWeek)) {
                        // Create a log for each time on this day
                        times.forEach { timeStr ->
                            val time = LocalTime.parse(timeStr)
                            val scheduledDateTime = LocalDateTime.of(date, time)
                            
                            // Only create logs for future times
                            if (scheduledDateTime.isAfter(LocalDateTime.now())) {
                                medicationRepository.logMedicationAdministration(
                                    medicationId = medicationId,
                                    scheduledTime = scheduledDateTime,
                                    administeredTime = null,
                                    administeredBy = null.toString(),
                                    status = TaskStatus.PENDING,
                                    notes = null
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
                _events.send(MedicationEvent.ShowMessage(e.message ?: "Error creating medication logs"))
            }
        }
    }
    
    fun logMedicationAdministration(
        medicationId: String,
        status: String,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                // In a real app, we would find the correct pending log and update it
                // For now, we'll create a new log entry for demonstration
                
                val administeredTime = if (status == "COMPLETED") LocalDateTime.now() else null

                (if (status == "COMPLETED") mockUserId else null)?.let {
                    medicationRepository.logMedicationAdministration(
                        medicationId = medicationId,
                        scheduledTime = LocalDateTime.now(),
                        administeredTime = administeredTime,
                        administeredBy = it,
                        status = TaskStatus.valueOf(status),
                        notes = notes
                    )
                }
                
                // Send event
                _events.send(MedicationEvent.MedicationLogged)
                
            } catch (e: Exception) {
                _events.send(MedicationEvent.ShowMessage(e.message ?: "Error logging medication"))
            }
        }
    }
    
    fun onEditMedication(medicationId: String) {
        viewModelScope.launch {
            _events.send(MedicationEvent.ShowMessage("Edit medication to be implemented"))
        }
    }
    
    private fun getDaysText(days: String): String {
        val daysList = days.split(",").map { it.toInt() }
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        return when {
            daysList.size == 7 -> "Every day"
            daysList.containsAll(listOf(1, 2, 3, 4, 5)) -> "Weekdays"
            daysList.containsAll(listOf(6, 7)) -> "Weekends"
            else -> daysList.map { daysOfWeek[it % 7] }.joinToString(", ")
        }
    }
}