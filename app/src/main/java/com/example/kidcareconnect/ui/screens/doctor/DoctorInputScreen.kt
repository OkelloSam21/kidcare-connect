package com.example.kidcareconnect.ui.screens.doctor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import com.example.kidcareconnect.ui.screens.child.HealthLogUi
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
            onAddHealthLog = { healthLog ->
                viewModel.addHealthLog(healthLog)
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
}

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

@Composable
fun HealthLogCard(log: HealthLogUi) {
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
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
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

@Composable
fun AddHealthLogDialog(
    onDismiss: () -> Unit,
    onAddHealthLog: (HealthLogFormData) -> Unit
) {
    var temperatureText by remember { mutableStateOf("") }
    var heartRateText by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Validation state
    val isTemperatureValid = temperatureText.isEmpty() || temperatureText.toFloatOrNull() != null
    val isHeartRateValid = heartRateText.isEmpty() || heartRateText.toIntOrNull() != null
    
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
                    text = "Add Health Record",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Temperature Field
                OutlinedTextField(
                    value = temperatureText,
                    onValueChange = { temperatureText = it },
                    label = { Text("Temperature (°F)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTemperatureValid,
                    supportingText = {
                        if (!isTemperatureValid) {
                            Text("Please enter a valid temperature")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Heart Rate Field
                OutlinedTextField(
                    value = heartRateText,
                    onValueChange = { heartRateText = it },
                    label = { Text("Heart Rate (bpm)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isHeartRateValid,
                    supportingText = {
                        if (!isHeartRateValid) {
                            Text("Please enter a valid heart rate")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Symptoms Field
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
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
                            onAddHealthLog(
                                HealthLogFormData(
                                    temperature = temperatureText.toFloatOrNull(),
                                    heartRate = heartRateText.toIntOrNull(),
                                    symptoms = symptoms.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                )
                            )
                        },
                        enabled = isTemperatureValid && isHeartRateValid && 
                                (temperatureText.isNotBlank() || heartRateText.isNotBlank() || 
                                 symptoms.isNotBlank() || notes.isNotBlank())
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onAddMedication: (MedicationFormData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDate by remember { mutableStateOf<String?>(null) }
    var frequency by remember { mutableStateOf("daily") }
    var priority by remember { mutableStateOf(MedicationPriority.MEDIUM) }
    var times by remember { mutableStateOf(listOf("09:00")) }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // Default to weekdays
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Prescribe Medication",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Medication Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                // Dosage
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                // Instructions
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    minLines = 2
                )
                
                // Date Range
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = endDate ?: "",
                        onValueChange = { 
                            endDate = it.ifBlank { null }
                        },
                        label = { Text("End Date (optional)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Frequency
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    val frequencies = listOf("daily", "every_other_day", "weekly", "as_needed")
                    val frequencyLabels = mapOf(
                        "daily" to "Daily",
                        "every_other_day" to "Every Other Day",
                        "weekly" to "Weekly",
                        "as_needed" to "As Needed (PRN)"
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequencies.forEach { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text(frequencyLabels[freq] ?: freq) }
                            )
                        }
                    }
                }
                
                // Priority
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    val priorities = MedicationPriority.entries
                    val priorityColors = mapOf(
                        MedicationPriority.CRITICAL to MaterialTheme.colorScheme.error,
                        MedicationPriority.HIGH to MaterialTheme.colorScheme.tertiary,
                        MedicationPriority.MEDIUM to MaterialTheme.colorScheme.secondary,
                        MedicationPriority.LOW to MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { prio ->
                            FilterChip(
                                selected = priority == prio,
                                onClick = { priority = prio },
                                label = { Text(prio.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = priorityColors[prio]?.copy(alpha = 0.2f)
                                        ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = priorityColors[prio]
                                        ?: MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
                
                // Times
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Times",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        IconButton(
                            onClick = { times = times + "12:00" }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Time")
                        }
                    }
                    
                    times.forEachIndexed { index, time ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = time,
                                onValueChange = { newTime ->
                                    times = times.toMutableList().apply {
                                        set(index, newTime)
                                    }
                                },
                                label = { Text("Time (HH:MM)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            
                            if (times.size > 1) {
                                IconButton(
                                    onClick = {
                                        times = times.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Time")
                                }
                            }
                        }
                    }
                }
                
                // Days
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Days",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
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
                    
                    Column {
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
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && dosage.isNotBlank() && selectedDays.isNotEmpty()) {
                                onAddMedication(
                                    MedicationFormData(
                                        name = name,
                                        dosage = dosage,
                                        instructions = instructions.ifBlank { null },
                                        startDate = startDate,
                                        endDate = endDate,
                                        frequency = frequency,
                                        priority = priority,
                                        times = times,
                                        days = selectedDays.joinToString(",")
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank() && dosage.isNotBlank() && selectedDays.isNotEmpty(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}