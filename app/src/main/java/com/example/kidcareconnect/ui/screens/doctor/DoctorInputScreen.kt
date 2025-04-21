package com.example.kidcareconnect.ui.screens.doctor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.ui.components.AddHealthLogDialog
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import com.example.kidcareconnect.ui.screens.child.HealthLogUi
import com.example.kidcareconnect.ui.screens.meal.MealScheduleFormData
import com.example.kidcareconnect.ui.screens.medication.AddMedicationDialog
import com.example.kidcareconnect.ui.screens.medication.MedicationFormData
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DoctorInputScreen(
    childId: String,
    navigateBack: () -> Unit,
    viewModel: DoctorInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddHealthLogDialog by remember { mutableStateOf(false) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var showAddDietaryPlanDialog by remember { mutableStateOf(false) }
    var showAssignCaretakerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DoctorInputEvent.HealthLogAdded -> {
                    showAddHealthLogDialog = false
                    snackbarHostState.showSnackbar("Health log added successfully")
                }
                is DoctorInputEvent.MedicationAdded -> {
                    showAddMedicationDialog = false
                    snackbarHostState.showSnackbar("Medication added successfully")
                }
                is DoctorInputEvent.DietaryPlanAdded -> {
                    showAddDietaryPlanDialog = false
                    snackbarHostState.showSnackbar("Dietary plan added successfully")
                }
                is DoctorInputEvent.CaretakerAssigned -> {
                    showAssignCaretakerDialog = false
                    snackbarHostState.showSnackbar("Caretaker assigned successfully")
                }
                is DoctorInputEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = "${uiState.childName}'s Medical Records",
                onBackClick = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isAdmin) {
                Column {
                    FloatingActionButton(
                        onClick = { showAssignCaretakerDialog = true },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Assign Caretaker"
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddDietaryPlanDialog = true },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Add Dietary Plan"
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddMedicationDialog = true },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "Add Medication"
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddHealthLogDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Add Health Record"
                        )
                    }
                }
            }
        }
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (!uiState.isAdmin) {
                        InfoCard(
                            title = "Doctor Input Area",
                            message = "This area is reserved for administrators and medical staff. " +
                                    "You can view health records below but cannot add new entries."
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Assigned Caretakers Section
                    if (uiState.isAdmin) {
                        Text(
                            text = "Assigned Caretakers",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        if (uiState.assignedCaretakers.isEmpty()) {
                            Text(
                                text = "No caretakers assigned yet",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                items(uiState.assignedCaretakers) { caretaker ->
                                    CaretakerItem(
                                        caretaker = caretaker,
                                        onRemove = { viewModel.removeCaretakerAssignment(caretaker.userId) }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Health Records Section
                    Text(
                        text = "Health Records",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (uiState.healthLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No health records available",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 8.dp)
                        ) {
                            items(uiState.healthLogs) { log ->
                                HealthLogCard(log = log)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Health Log Dialog
    if (showAddHealthLogDialog) {
        AddHealthLogDialog(
            onDismiss = { showAddHealthLogDialog = false },
            onAddHealthLog = {
                viewModel.addHealthLog(it)
            }
        )
    }

    // Add Medication Dialog
    if (showAddMedicationDialog) {
        AddMedicationDialog(
            onDismiss = { showAddMedicationDialog = false },
            onAddMedication = { medication ->
                viewModel.addMedication(medication)
            }
        )
    }

    // Add Dietary Plan Dialog
    if (showAddDietaryPlanDialog) {
        AddDietaryPlanDialog(
            onDismiss = { showAddDietaryPlanDialog = false },
            onAddDietaryPlan = { allergies, restrictions, preferences, notes, mealSchedules ->
                viewModel.addDietaryPlan(allergies, restrictions, preferences, notes, mealSchedules)
            }
        )
    }

    // Assign Caretaker Dialog
    if (showAssignCaretakerDialog) {
        AssignCaretakerDialog(
            caretakers = uiState.caretakers.filter { caretaker ->
                !uiState.assignedCaretakers.any { it.userId == caretaker.userId }
            },
            onDismiss = { showAssignCaretakerDialog = false },
            onAssignCaretaker = { caretakerId ->
                viewModel.assignCaretaker(caretakerId)
            }
        )
    }
}

@Composable
fun CaretakerItem(
    caretaker: UserEntity,
    onRemove: () -> Unit
) {
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
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = caretaker.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = caretaker.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Caretaker",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddDietaryPlanDialog(
    onDismiss: () -> Unit,
    onAddDietaryPlan: (String?, String?, String?, String?, List<MealScheduleFormData>) -> Unit
) {
    var allergies by remember { mutableStateOf("") }
    var restrictions by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Meal schedule state
    var mealSchedules by remember {
        mutableStateOf(
            listOf(
                MealScheduleFormData(
                    mealType = MealType.BREAKFAST,
                    time = "08:00",
                    days = listOf(1, 2, 3, 4, 5, 6, 7),
                    menu = "Cereal with fruit"
                ),
                MealScheduleFormData(
                    mealType = MealType.LUNCH,
                    time = "12:00",
                    days = listOf(1, 2, 3, 4, 5, 6, 7),
                    menu = "Balanced meal with protein and vegetables"
                )
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Dietary Plan",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Dietary Information Section
                Text(
                    text = "Dietary Information",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = restrictions,
                    onValueChange = { restrictions = it },
                    label = { Text("Dietary Restrictions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = preferences,
                    onValueChange = { preferences = it },
                    label = { Text("Food Preferences") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp) )

                // Meal Schedules Section
                Text(
                    text = "Meal Schedules",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Meal schedule inputs would go here
                // For simplicity, we'll just use a text field to indicate this
                Text(
                    text = "Default meal schedules will be created for breakfast and lunch",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onAddDietaryPlan(
                                allergies.ifBlank { null },
                                restrictions.ifBlank { null },
                                preferences.ifBlank { null },
                                notes.ifBlank { null },
                                mealSchedules
                            )
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AssignCaretakerDialog(
    caretakers: List<UserEntity>,
    onDismiss: () -> Unit,
    onAssignCaretaker: (String) -> Unit
) {
    var selectedCaretakerId by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Assign Caretaker",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (caretakers.isEmpty()) {
                    Text(
                        text = "No available caretakers to assign",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // List of available caretakers
                    caretakers.forEach { caretaker ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCaretakerId == caretaker.userId,
                                    onClick = { selectedCaretakerId = caretaker.userId },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCaretakerId == caretaker.userId,
                                onClick = null
                            )

                            Column(
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(
                                    text = caretaker.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = caretaker.email,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            selectedCaretakerId?.let { onAssignCaretaker(it) }
                        },
                        enabled = selectedCaretakerId != null,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Assign")
                    }
                }
            }
        }
    }
}

/**
 * Information card used in various screens to provide context or instructions
 */
@Composable
fun InfoCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Card to display health log information
 */
@Composable
fun HealthLogCard(
    log: HealthLogUi
) {
    Card(
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (log.temperature != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Temperature",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${log.temperature}°F",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (log.heartRate != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Heart Rate",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${log.heartRate} bpm",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (!log.symptoms.isNullOrEmpty()) {
                Text(
                    text = "Symptoms:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                Text(
                    text = log.symptoms,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!log.notes.isNullOrEmpty()) {
                Text(
                    text = "Notes:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}