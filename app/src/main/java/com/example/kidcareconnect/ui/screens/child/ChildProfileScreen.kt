package com.example.kidcareconnect.ui.screens.child

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import com.example.kidcareconnect.ui.components.TaskCard
import com.example.kidcareconnect.ui.screens.dashboard.PendingTaskUi
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChildProfileScreen(
    childId: String,
    navigateToMedication: () -> Unit,
    navigateToMealPlanning: () -> Unit,
    navigateToDoctorInput: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: ChildProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Medications", "Meals", "Health", "Notes")

    LaunchedEffect(key1 = childId) {
        viewModel.loadChildData(childId)
    }

    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChildProfileEvent.NavigateToMedication -> navigateToMedication()
                is ChildProfileEvent.NavigateToMealPlanning -> navigateToMealPlanning()
                is ChildProfileEvent.NavigateToDoctorInput -> navigateToDoctorInput()
                is ChildProfileEvent.ShowMessage -> {
                    // Handle showing messages (e.g., Snackbar)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = uiState.child?.name ?: "Child Profile",
                onBackClick = navigateBack,
                actions = {
                    if (uiState.isAdmin) {
                        IconButton(onClick = { viewModel.onEditProfileClicked() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.currentUserRole == UserRole.ADMIN) {
                when (selectedTabIndex) {
                    0 -> { // Medications tab
                        FloatingActionButton(
                            onClick = { viewModel.onAddMedicationClicked() }
                        ) {
                            Icon(Icons.Default.Add, "Add medication")
                        }

                    }

                    1 -> { // Meals tab
                        FloatingActionButton(
                            onClick = { viewModel.onAddMealClicked() }
                        ) {
                            Icon(Icons.Default.Add, "Add meal plan")
                        }

                    }

                    2 -> { // Health tab
                        FloatingActionButton(
                            onClick = { viewModel.onAddHealthLogClicked() }
                        ) {
                            Icon(Icons.Default.Add, "Add health log")
                        }
                    }

                    3 -> { // Notes tab
                        FloatingActionButton(
                            onClick = { viewModel.onAddNoteClicked() }
                        ) {
                            Icon(Icons.Default.Add, "Add note")
                        }

                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Extract child to a local variable to help with smart casting
            val child = uiState.child

            if (child == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Child not found")
                }
            } else {
                // Now we can safely use the non-null child
                ChildProfileContent(
                    child = child,
                    uiState = uiState,
                    selectedTabIndex = selectedTabIndex,
                    tabs = tabs,
                    onTabSelected = { selectedTabIndex = it },
                    onMedicationSelected = viewModel::onMedicationSelected,
                    onMealSelected = viewModel::onMealSelected,
                    onHealthLogSelected = viewModel::onHealthLogSelected,
                    onNoteSelected = viewModel::onNoteSelected,
                    onTaskSelected = viewModel::onTaskSelected,
                    calculateAge = viewModel::calculateAge,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ChildProfileContent(
    child: Child,
    uiState: ChildProfileUiState,
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    onMedicationSelected: (String) -> Unit,
    onMealSelected: (String) -> Unit,
    onHealthLogSelected: (String) -> Unit,
    onNoteSelected: (String) -> Unit,
    onTaskSelected: (PendingTaskUi) -> Unit,
    calculateAge: (java.time.LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Child header information
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (child.profilePictureUrl != null) {
                    Text(
                        text = child.name.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = child.name.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Child information
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Age: ${calculateAge(child.dateOfBirth)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Gender: ${child.gender}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (child.bloodGroup != null) {
                    Text(
                        text = "Blood Group: ${child.bloodGroup}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Upcoming tasks at the top
        if (uiState.upcomingTasks.isNotEmpty()) {
            Text(
                text = "Upcoming Tasks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 8.dp)
            ) {
                items(uiState.upcomingTasks) { task ->
                    TaskCard(
                        title = task.title,
                        description = task.description,
                        icon = {
                            Icon(
                                imageVector = when (task.type) {
                                    "medication" -> Icons.Default.Medication
                                    "meal" -> Icons.Default.Restaurant
                                    "health" -> Icons.Default.HealthAndSafety
                                    else -> Icons.AutoMirrored.Filled.Assignment
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        priority = task.priority,
                        onClick = { onTaskSelected(task) }
                    )
                }
            }
        }

        // Tabs for different sections
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.Medication
                                1 -> Icons.Default.Restaurant
                                2 -> Icons.Default.HealthAndSafety
                                3 -> Icons.AutoMirrored.Filled.Notes
                                else -> Icons.Default.Info
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }

        // Tab content
        when (selectedTabIndex) {
            0 -> MedicationsTab(
                medications = uiState.medications,
                onMedicationClick = onMedicationSelected
            )

            1 -> MealsTab(
                dietaryPlan = uiState.dietaryPlan,
                mealLogs = uiState.mealLogs,
                onMealClick = onMealSelected
            )

            2 -> HealthTab(
                healthLogs = uiState.healthLogs,
                onHealthLogClick = onHealthLogSelected
            )

            3 -> NotesTab(
                notes = uiState.notes,
                onNoteClick = onNoteSelected
            )
        }
    }
}

@Composable
fun MedicationsTab(
    medications: List<MedicationUi>,
    onMedicationClick: (String) -> Unit
) {
    if (medications.isEmpty()) {
        EmptyTabContent(
            icon = Icons.Default.Medication,
            message = "No medications scheduled"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            items(medications) { medication ->
                Card(
                    onClick = { onMedicationClick(medication.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Dosage: ${medication.dosage}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Schedule: ${medication.schedule}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (medication.instructions != null) {
                            Text(
                                text = "Instructions: ${medication.instructions}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val priorityColor = when (medication.priority) {
                                "CRITICAL" -> MaterialTheme.colorScheme.error
                                "HIGH" -> MaterialTheme.colorScheme.tertiary
                                "MEDIUM" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = priorityColor)
                            ) {
                                Text(
                                    text = medication.priority,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealsTab(
    dietaryPlan: DietaryPlanUi?,
    mealLogs: List<MealLogUi>,
    onMealClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (dietaryPlan == null) {
            EmptyTabContent(
                icon = Icons.Default.Restaurant,
                message = "No dietary plan set"
            )
        } else {
            // Dietary Plan Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
                }
            }

            // Meal Logs
            Text(
                text = "Recent Meals",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (mealLogs.isEmpty()) {
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
                LazyColumn {
                    items(mealLogs) { mealLog ->
                        Card(
                            onClick = { onMealClick(mealLog.id) },
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
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(end = 16.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mealLog.mealType,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = mealLog.time,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (mealLog.notes != null) {
                                        Text(
                                            text = mealLog.notes,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                // Status indicator
                                val statusColor = when (mealLog.status) {
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
                }
            }
        }
    }
}

@Composable
fun HealthTab(
    healthLogs: List<HealthLogUi>,
    onHealthLogClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Health Records",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (healthLogs.isEmpty()) {
            EmptyTabContent(
                icon = Icons.Default.HealthAndSafety,
                message = "No health logs available"
            )
        } else {
            LazyColumn {
                items(healthLogs) { log ->
                    Card(
                        onClick = { onHealthLogClick(log.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.date,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = log.time,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (log.temperature != null) {
                                    HealthMetricChip(
                                        icon = Icons.Default.Thermostat,
                                        label = "Temp",
                                        value = "${log.temperature}°F"
                                    )
                                }

                                if (log.heartRate != null) {
                                    HealthMetricChip(
                                        icon = Icons.Default.Favorite,
                                        label = "Heart Rate",
                                        value = "${log.heartRate} bpm"
                                    )
                                }
                            }

                            if (!log.symptoms.isNullOrEmpty()) {
                                Text(
                                    text = "Symptoms: ${log.symptoms}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            if (!log.notes.isNullOrEmpty()) {
                                Text(
                                    text = log.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun NotesTab(
    notes: List<NoteUi>,
    onNoteClick: (String) -> Unit
) {
    if (notes.isEmpty()) {
        EmptyTabContent(
            icon = Icons.AutoMirrored.Filled.Notes,
            message = "No notes available"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(notes) { note ->
                Card(
                    onClick = { onNoteClick(note.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = note.date,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "By: ${note.author}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTabContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}