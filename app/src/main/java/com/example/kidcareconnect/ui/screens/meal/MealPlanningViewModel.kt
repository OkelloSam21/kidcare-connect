package com.example.kidcareconnect.ui.screens.meal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.TaskStatus
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.data.repository.ChildRepository
import com.example.kidcareconnect.data.repository.DietaryRepository
import com.example.kidcareconnect.data.repository.UserRepository
import com.example.kidcareconnect.ui.screens.child.DietaryPlanUi
import com.example.kidcareconnect.ui.screens.child.MealLogUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Events from Meal Planning Screen
sealed class MealPlanningEvent {
    object MealPlanAdded : MealPlanningEvent()
    object MealPlanUpdated : MealPlanningEvent()
    object MealLogged : MealPlanningEvent()
    data class ShowMessage(val message: String) : MealPlanningEvent()
}

// UI State for Meal Planning Screen
data class MealPlanningUiState(
    val childId: String = "",
    val childName: String = "",
    val dietaryPlan: DietaryPlanUi? = null,
    val mealLogs: List<MealLogUi> = emptyList(),
    val upcomingMeals: List<UpcomingMealUi> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// UI model for Upcoming Meal
data class UpcomingMealUi(
    val id: String,
    val mealType: String,
    val time: String,
    val menu: String?,
    val days: String,
    val status: TaskStatus = TaskStatus.PENDING
)

// UI model for Meal Schedule Form
data class MealScheduleFormData(
    val mealType: MealType,
    val time: String,
    val days: List<Int>,
    val menu: String?
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class MealPlanningViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val dietaryRepository: DietaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()
{
    
    private val _uiState = MutableStateFlow(MealPlanningUiState())
    val uiState: StateFlow<MealPlanningUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<MealPlanningEvent>()
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
                        
                        // Load meal schedules for this plan
                        loadMealSchedules(dietaryPlan.planId)
                        
                        // Load meal logs
                        dietaryRepository.getMealLogsForChild(childId).collect { mealLogs ->
                            val mealLogsUi = mealLogs.map { log ->
                                MealLogUi(
                                    id = log.logId,
                                    mealType = getMealTypeName(log.scheduleId), // This is a mock function
                                    time = formatDateTime(log.scheduledTime),
                                    status = log.status.name,
                                    notes = log.notes
                                )
                            }
                            
                            _uiState.update { state ->
                                state.copy(
                                    mealLogs = mealLogsUi,
                                    isLoading = false
                                )
                            }
                        }
                    } else {
                        _uiState.update { state ->
                            state.copy(isLoading = false)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading meal planning data"
                    )
                }
            }
        }
    }
    
    private fun loadMealSchedules(planId: String) {
        viewModelScope.launch {
            try {
                dietaryRepository.getMealSchedulesForPlan(planId).collect { schedules ->
                    val upcomingMeals = schedules.map { schedule ->
                        UpcomingMealUi(
                            id = schedule.scheduleId,
                            mealType = schedule.mealType.name,
                            time = schedule.time,
                            menu = schedule.menu,
                            days = getDaysText(schedule.days),
                            status = TaskStatus.PENDING
                        )
                    }
                    
                    _uiState.update { state ->
                        state.copy(upcomingMeals = upcomingMeals)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(errorMessage = e.message ?: "Error loading meal schedules")
                }
            }
        }
    }
    
    fun createDietaryPlan(
        allergies: String?,
        restrictions: String?,
        preferences: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                // Only admins can create dietary plans
                if (!_uiState.value.isAdmin) {
                    _events.send(MealPlanningEvent.ShowMessage("Only administrators can create dietary plans"))
                    return@launch
                }
                
                val childId = _uiState.value.childId
                val plan = dietaryRepository.createDietaryPlan(
                    childId = childId,
                    allergies = allergies,
                    restrictions = restrictions,
                    preferences = preferences,
                    notes = notes,
                    createdBy = mockUserId
                )
                
                // Load updated data
                loadChildData(childId)
                
                _events.send(MealPlanningEvent.MealPlanAdded)
            } catch (e: Exception) {
                _events.send(MealPlanningEvent.ShowMessage(e.message ?: "Error creating dietary plan"))
            }
        }
    }
    
    fun addMealSchedule(mealSchedule: MealScheduleFormData) {
        viewModelScope.launch {
            try {
                // Only admins can add meal schedules
                if (!_uiState.value.isAdmin) {
                    _events.send(MealPlanningEvent.ShowMessage("Only administrators can add meal schedules"))
                    return@launch
                }
                
                val dietaryPlan = _uiState.value.dietaryPlan
                if (dietaryPlan != null) {
                    dietaryRepository.createMealSchedule(
                        planId = dietaryPlan.id,
                        mealType = mealSchedule.mealType,
                        time = mealSchedule.time,
                        days = mealSchedule.days.joinToString(","),
                        menu = mealSchedule.menu
                    )
                    
                    // Reload meal schedules
                    loadMealSchedules(dietaryPlan.id)
                    
                    _events.send(MealPlanningEvent.MealPlanUpdated)
                } else {
                    _events.send(MealPlanningEvent.ShowMessage("No dietary plan exists. Please create one first."))
                }
            } catch (e: Exception) {
                _events.send(MealPlanningEvent.ShowMessage(e.message ?: "Error adding meal schedule"))
            }
        }
    }
    
    fun logMealService(
        scheduleId: String,
        status: TaskStatus,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                // Both admins and caretakers can log meals
                val now = LocalDateTime.now()
                
                dietaryRepository.logMealService(
                    scheduleId = scheduleId,
                    scheduledTime = now,
                    servedTime = if (status == TaskStatus.COMPLETED) now else null,
                    servedBy = if (status == TaskStatus.COMPLETED) mockUserId else null,
                    status = status,
                    notes = notes
                )
                
                // Reload meal logs
                loadChildData(_uiState.value.childId)
                
                _events.send(MealPlanningEvent.MealLogged)
            } catch (e: Exception) {
                _events.send(MealPlanningEvent.ShowMessage(e.message ?: "Error logging meal service"))
            }
        }
    }
    
    // Mock function to get meal type name from schedule ID
    private fun getMealTypeName(scheduleId: String): String {
        // This is a simplified mock implementation
        // In a real app, we would fetch the meal type from the database
        return MealType.entries.random().name
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
    
    private fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
        return dateTime.format(formatter)
    }
}
