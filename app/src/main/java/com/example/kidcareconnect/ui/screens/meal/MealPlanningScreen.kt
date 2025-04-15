package com.example.kidcareconnect.ui.screens.meal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.TaskStatus
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import com.example.kidcareconnect.ui.screens.child.DietaryPlanUi
import com.example.kidcareconnect.ui.screens.child.MealLogUi
import kotlinx.coroutines.flow.*
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealPlanningScreen(
    childId: String,
    navigateBack: () -> Unit,
    viewModel: MealPlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showAddDietaryPlanDialog by remember { mutableStateOf(false) }
    var showAddMealScheduleDialog by remember { mutableStateOf(false) }
    var showLogMealDialog by remember { mutableStateOf(false) }
    var selectedMealId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MealPlanningEvent.MealPlanAdded -> {
                    showAddDietaryPlanDialog = false
                    snackbarHostState.showSnackbar("Dietary plan added successfully")
                }
                is MealPlanningEvent.MealPlanUpdated -> {
                    showAddMealScheduleDialog = false
                    snackbarHostState.showSnackbar("Meal schedule updated")
                }
                is MealPlanningEvent.MealLogged -> {
                    showLogMealDialog = false
                    snackbarHostState.showSnackbar("Meal logged successfully")
                }
                is MealPlanningEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = "${uiState.childName}'s Meal Planning",
                onBackClick = navigateBack,
                actions = {
                    if (uiState.isAdmin) {
                        if (uiState.dietaryPlan == null) {
                            IconButton(onClick = { showAddDietaryPlanDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Dietary Plan"
                                )
                            }
                        } else {
                            IconButton(onClick = { showAddMealScheduleDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddTask,
                                    contentDescription = "Add Meal Schedule"
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.dietaryPlan == null) {
                NoDietaryPlanView(
                    isAdmin = uiState.isAdmin,
                    onAddDietaryPlan = { showAddDietaryPlanDialog = true }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Dietary Plan Card
                    DietaryPlanCard(dietaryPlan = uiState.dietaryPlan!!)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Upcoming Meals Section
                    Text(
                        text = "Meal Schedules",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (uiState.upcomingMeals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No meal schedules found",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxWidth()
                        ) {
                            items(uiState.upcomingMeals) { meal ->
                                MealScheduleItem(
                                    meal = meal,
                                    onClick = {
                                        selectedMealId = meal.id
                                        showLogMealDialog = true
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Meal Logs Section
                    Text(
                        text = "Recent Meal Logs",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (uiState.mealLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No meal logs available",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxWidth()
                        ) {
                            items(uiState.mealLogs) { log ->
                                MealLogItem(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Dietary Plan Dialog
    if (showAddDietaryPlanDialog) {
        AddDietaryPlanDialog(
            onDismiss = { showAddDietaryPlanDialog = false },
            onAddDietaryPlan = { allergies, restrictions, preferences, notes ->
                viewModel.createDietaryPlan(allergies, restrictions, preferences, notes)
            }
        )
    }
    
    // Add Meal Schedule Dialog
    if (showAddMealScheduleDialog) {
        AddMealScheduleDialog(
            onDismiss = { showAddMealScheduleDialog = false },
            onAddMealSchedule = { mealSchedule ->
                viewModel.addMealSchedule(mealSchedule)
            }
        )
    }
    
    // Log Meal Dialog
    if (showLogMealDialog && selectedMealId != null) {
        val selectedMeal = uiState.upcomingMeals.find { it.id == selectedMealId }
        selectedMeal?.let {
            LogMealDialog(
                meal = selectedMeal,
                onDismiss = { showLogMealDialog = false },
                onLogMeal = { status, notes ->
                    viewModel.logMealService(selectedMealId!!, status, notes)
                }
            )
        }
    }
}

@Composable
fun NoDietaryPlanView(
    isAdmin: Boolean,
    onAddDietaryPlan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Text(
            text = "No Dietary Plan",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        Text(
            text = "This child doesn't have a dietary plan set up yet.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        if (isAdmin) {
            Button(
                onClick = onAddDietaryPlan
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Dietary Plan")
            }
        } else {
            Text(
                text = "Please contact an administrator to set up a dietary plan.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun DietaryPlanCard(dietaryPlan: DietaryPlanUi) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dietary Information",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (!dietaryPlan.allergies.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Allergies: ${dietaryPlan.allergies}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            if (!dietaryPlan.restrictions.isNullOrEmpty()) {
                Text(
                    text = "Restrictions: ${dietaryPlan.restrictions}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (!dietaryPlan.preferences.isNullOrEmpty()) {
                Text(
                    text = "Preferences: ${dietaryPlan.preferences}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (!dietaryPlan.notes.isNullOrEmpty()) {
                Text(
                    text = "Notes: ${dietaryPlan.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MealScheduleItem(
    meal: UpcomingMealUi,
    onClick: () -> Unit
) {
    val mealTypeIcon = when (meal.mealType) {
        "BREAKFAST" -> Icons.Default.FreeBreakfast
        "LUNCH" -> Icons.Default.LunchDining
        "DINNER" -> Icons.Default.DinnerDining
        else -> Icons.Default.Restaurant
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = mealTypeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.mealType.replace("_", " ").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = "Time: ${meal.time}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Days: ${meal.days}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (meal.menu != null) {
                    Text(
                        text = "Menu: ${meal.menu}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PlaylistAddCheck,
                    contentDescription = "Log Meal"
                )
            }
        }
    }
}

@Composable
fun MealLogItem(log: MealLogUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mealTypeIcon = when (log.mealType) {
                "BREAKFAST" -> Icons.Default.FreeBreakfast
                "LUNCH" -> Icons.Default.LunchDining
                "DINNER" -> Icons.Default.DinnerDining
                else -> Icons.Default.Restaurant
            }
            
            Icon(
                imageVector = mealTypeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.mealType.replace("_", " ").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = log.time,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (log.notes != null) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Status indicator
            val statusColor = when (log.status) {
                "COMPLETED" -> MaterialTheme.colorScheme.tertiary
                "MISSED" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
            
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = statusColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun AddDietaryPlanDialog(
    onDismiss: () -> Unit,
    onAddDietaryPlan: (String?, String?, String?, String?) -> Unit
) {
    var allergies by remember { mutableStateOf("") }
    var restrictions by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Dietary Plan",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = restrictions,
                    onValueChange = { restrictions = it },
                    label = { Text("Dietary Restrictions") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = preferences,
                    onValueChange = { preferences = it },
                    label = { Text("Food Preferences") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            onAddDietaryPlan(
                                allergies.ifBlank { null },
                                restrictions.ifBlank { null },
                                preferences.ifBlank { null },
                                notes.ifBlank { null }
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMealScheduleDialog(
    onDismiss: () -> Unit,
    onAddMealSchedule: (MealScheduleFormData) -> Unit
) {
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var time by remember { mutableStateOf("08:00") }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // Default to weekdays
    var menu by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Meal Schedule",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Type Selection
                Text(
                    text = "Meal Type",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { mealType ->
                        FilterChip(
                            selected = selectedMealType == mealType,
                            onClick = { selectedMealType = mealType },
                            label = { 
                                Text(
                                    mealType.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (mealType) {
                                        MealType.BREAKFAST -> Icons.Default.FreeBreakfast
                                        MealType.LUNCH -> Icons.Default.LunchDining
                                        MealType.DINNER -> Icons.Default.DinnerDining
                                        else -> Icons.Default.Restaurant
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time Input
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Days Selection
                Text(
                    text = "Days",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                val daysOfWeek = listOf(
                    "Monday" to 1,
                    "Tuesday" to 2,
                    "Wednesday" to 3,
                    "Thursday" to 4,
                    "Friday" to 5,
                    "Saturday" to 6,
                    "Sunday" to 7
                )
                
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    daysOfWeek.forEach { (dayName, dayValue) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedDays.contains(dayValue),
                                onCheckedChange = { checked ->
                                    selectedDays = if (checked) {
                                        selectedDays + dayValue
                                    } else {
                                        selectedDays - dayValue
                                    }
                                }
                            )
                            
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Menu Input
                OutlinedTextField(
                    value = menu,
                    onValueChange = { menu = it },
                    label = { Text("Menu (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (selectedDays.isNotEmpty()) {
                                onAddMealSchedule(
                                    MealScheduleFormData(
                                        mealType = selectedMealType,
                                        time = time,
                                        days = selectedDays.toList(),
                                        menu = menu.ifBlank { null }
                                    )
                                )
                            }
                        },
                        enabled = selectedDays.isNotEmpty() && time.matches(Regex("\\d{1,2}:\\d{2}"))
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun LogMealDialog(
    meal: UpcomingMealUi,
    onDismiss: () -> Unit,
    onLogMeal: (TaskStatus, String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(TaskStatus.COMPLETED) }
    var notes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Log Meal",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = meal.mealType.replace("_", " ").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Time: ${meal.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (meal.menu != null) {
                    Text(
                        text = "Menu: ${meal.menu}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status Selection
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    TaskStatus.entries.forEach { status ->
                        if (status != TaskStatus.PENDING) { // Skip PENDING since we're logging an action
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStatus == status,
                                    onClick = { selectedStatus = status }
                                )
                                
                                Text(
                                    text = when (status) {
                                        TaskStatus.COMPLETED -> "Served"
                                        TaskStatus.MISSED -> "Not Served"
                                        TaskStatus.RESCHEDULED -> "Rescheduled"
                                        else -> status.name
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            onLogMeal(selectedStatus, notes.ifBlank { null })
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}