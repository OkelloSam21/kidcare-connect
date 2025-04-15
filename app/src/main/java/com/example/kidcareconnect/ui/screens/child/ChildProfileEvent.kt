package com.example.kidcareconnect.ui.screens.child

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.ChildRepository
import com.example.kidcareconnect.data.repository.DietaryRepository
import com.example.kidcareconnect.data.repository.HealthRepository
import com.example.kidcareconnect.data.repository.MedicationRepository
import com.example.kidcareconnect.data.repository.UserRepository
import com.example.kidcareconnect.ui.screens.dashboard.PendingTaskUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Events from Child Profile Screen
sealed class ChildProfileEvent {
    object NavigateToMedication : ChildProfileEvent()
    object NavigateToMealPlanning : ChildProfileEvent()
    object NavigateToDoctorInput : ChildProfileEvent()
    data class ShowMessage(val message: String) : ChildProfileEvent()
}

// UI models for Child Profile sections
data class MedicationUi(
    val id: String,
    val name: String,
    val dosage: String,
    val schedule: String,
    val instructions: String?,
    val priority: String
)

data class DietaryPlanUi(
    val id: String,
    val allergies: String?,
    val restrictions: String?,
    val preferences: String?,
    val notes: String?
)

data class MealLogUi(
    val id: String,
    val mealType: String,
    val time: String,
    val status: String,
    val notes: String?
)

data class HealthLogUi(
    val id: String,
    val date: String,
    val time: String,
    val temperature: Float?,
    val heartRate: Int?,
    val symptoms: String?,
    val notes: String?
)

data class NoteUi(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val author: String
)

// UI State for Child Profile
data class ChildProfileUiState(
    val child: Child? = null,
    val medications: List<MedicationUi> = emptyList(),
    val dietaryPlan: DietaryPlanUi? = null,
    val mealLogs: List<MealLogUi> = emptyList(),
    val healthLogs: List<HealthLogUi> = emptyList(),
    val notes: List<NoteUi> = emptyList(),
    val upcomingTasks: List<PendingTaskUi> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChildProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val medicationRepository: MedicationRepository,
    private val dietaryRepository: DietaryRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChildProfileUiState())
    val uiState: StateFlow<ChildProfileUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<ChildProfileEvent>()
    val events = _events.receiveAsFlow()
    
    // Mock current user for development
    private val mockUserId = "user1"
    
    init {
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
    
    fun loadChildData(childId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load basic child info
                val child = childRepository.getChildById(childId)
                child?.let {
                    _uiState.update { state ->
                        state.copy(child = it)
                    }
                }
                
                // Load medications
                medicationRepository.getMedicationsForChild(childId).collect { medications ->
                    val medicationUiList = medications.map { medication ->
                        // Get schedules for each medication to display
                        val schedules = mutableListOf<String>()
                        medicationRepository.getSchedulesForMedication(medication.medicationId).collect { scheduleList ->
                            scheduleList.forEach { schedule ->
                                schedules.add("${getDaysText(schedule.days)} at ${schedule.time}")
                            }
                        }
                        
                        MedicationUi(
                            id = medication.medicationId,
                            name = medication.name,
                            dosage = medication.dosage,
                            schedule = schedules.joinToString(", "),
                            instructions = medication.instructions,
                            priority = medication.priority.name
                        )
                    }
                    
                    _uiState.update { state ->
                        state.copy(medications = medicationUiList)
                    }
                }
                
                // Load dietary plan
                dietaryRepository.getDietaryPlanForChild(childId).collect { dietaryPlan ->
                    if (dietaryPlan != null) {
                        val dietaryPlanUi = DietaryPlanUi(
                            id = dietaryPlan.planId,
                            allergies = dietaryPlan.allergies,
                            restrictions = dietaryPlan.restrictions,
                            preferences = dietaryPlan.preferences,
                            notes = dietaryPlan.notes
                        )
                        
                        _uiState.update { state ->
                            state.copy(dietaryPlan = dietaryPlanUi)
                        }
                        
                        // Load meal logs for this dietary plan
                        dietaryRepository.getMealLogsForChild(childId).collect { mealLogs ->
                            val mealLogsUi = mealLogs.map { log ->
                                MealLogUi(
                                    id = log.logId,
                                    mealType = getMealTypeName(log.scheduleId), // We would need to get the meal type from the schedule
                                    time = formatDateTime(log.scheduledTime),
                                    status = log.status.name,
                                    notes = log.notes
                                )
                            }
                            
                            _uiState.update { state ->
                                state.copy(mealLogs = mealLogsUi)
                            }
                        }
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
                        state.copy(healthLogs = healthLogsUi)
                    }
                }
                
                // Load upcoming tasks (pending medication and meal logs)
                loadUpcomingTasks(childId)
                
                // Load mock notes for now
                val mockNotes = listOf(
                    NoteUi(
                        id = "note1",
                        title = "Initial Assessment",
                        content = "Child is healthy and developing normally for their age.",
                        date = "Apr 10, 2025",
                        author = "Dr. Johnson"
                    ),
                    NoteUi(
                        id = "note2",
                        title = "Behavior Observation",
                        content = "Shows good social interaction with other children. Participates actively in group activities.",
                        date = "Apr 8, 2025",
                        author = "Ms. Williams (Teacher)"
                    )
                )
                
                _uiState.update { state ->
                    state.copy(
                        notes = mockNotes,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading child data"
                    )
                }
            }
        }
    }
    
    private fun loadUpcomingTasks(childId: String) {
        viewModelScope.launch {
            val tasks = mutableListOf<PendingTaskUi>()
            
            // Medication tasks
            medicationRepository.getPendingMedicationsForChild(childId).collect { logs ->
                logs.forEach { log ->
                    // Get medication details
                    medicationRepository.getMedicationsForChild(childId).collect { medications ->
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
                                    childId = childId,
                                    childName = _uiState.value.child?.name ?: "",
                                    title = "Give ${medication.name}",
                                    description = "Dose: ${medication.dosage}",
                                    time = formatDateTime(log.scheduledTime),
                                    type = "medication",
                                    priority = priority
                                )
                            )
                        }
                    }
                }
                
                // TODO: Add meal tasks in similar fashion
                
                // Sort tasks by priority (high to low) and then by time
                val sortedTasks = tasks.sortedWith(
                    compareByDescending<PendingTaskUi> { it.priority }
                        .thenBy { it.time }
                )
                
                _uiState.update { state ->
                    state.copy(upcomingTasks = sortedTasks)
                }
            }
        }
    }


    fun calculateAge(dateOfBirth: LocalDate): String {
        val now = LocalDate.now()
        val period = Period.between(dateOfBirth, now)
        return when {
            period.years > 0 -> "${period.years} yr${if (period.years > 1) "s" else ""}"
            period.months > 0 -> "${period.months} mo${if (period.months > 1) "s" else ""}"
            else -> "${period.days} day${if (period.days > 1) "s" else ""}"
        }
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
        return dateTime.format(formatter)
    }

    private fun formatDate(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        return dateTime.format(formatter)
    }

    private fun formatTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return dateTime.format(formatter)
    }
    
    private fun getDaysText(days: String): String {
        val daysList = days.split(",").map { it.toInt() }
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        return when {
            daysList.size == 7 -> "Every day"
            daysList.containsAll(listOf(1, 2, 3, 4, 5)) -> "Weekdays"
            daysList.containsAll(listOf(6, 7)) -> "Weekends"
            else -> daysList.map { daysOfWeek[it - 1] }.joinToString(", ")
        }
    }
    
    private fun getMealTypeName(scheduleId: String): String {
        // This is a mock implementation - in a real app, we would fetch the meal type
        // from the database based on the schedule ID
        return listOf("Breakfast", "Lunch", "Snack", "Dinner").random()
    }
    
    fun onMedicationSelected(medicationId: String) {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.NavigateToMedication)
        }
    }
    
    fun onMealSelected(mealLogId: String) {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.NavigateToMealPlanning)
        }
    }
    
    fun onHealthLogSelected(healthLogId: String) {
        // View health log details
        viewModelScope.launch {
            _events.send(ChildProfileEvent.ShowMessage("Health log details to be implemented"))
        }
    }
    
    fun onNoteSelected(noteId: String) {
        // View note details
        viewModelScope.launch {
            _events.send(ChildProfileEvent.ShowMessage("Note details to be implemented"))
        }
    }
    
    fun onTaskSelected(task: PendingTaskUi) {
        when (task.type) {
            "medication" -> viewModelScope.launch { _events.send(ChildProfileEvent.NavigateToMedication) }
            "meal" -> viewModelScope.launch { _events.send(ChildProfileEvent.NavigateToMealPlanning) }
            else -> viewModelScope.launch { _events.send(ChildProfileEvent.ShowMessage("Task action to be implemented")) }
        }
    }
    
    fun onEditProfileClicked() {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.ShowMessage("Edit profile to be implemented"))
        }
    }
    
    fun onAddMedicationClicked() {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.NavigateToMedication)
        }
    }
    
    fun onAddMealClicked() {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.NavigateToMealPlanning)
        }
    }
    
    fun onAddHealthLogClicked() {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.ShowMessage("Add health log to be implemented"))
        }
    }
    
    fun onAddNoteClicked() {
        viewModelScope.launch {
            _events.send(ChildProfileEvent.ShowMessage("Add note to be implemented"))
        }
    }
}