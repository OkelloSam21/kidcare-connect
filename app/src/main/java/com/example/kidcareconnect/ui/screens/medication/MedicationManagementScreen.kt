package com.example.kidcareconnect.ui.screens.medication

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MedicationManagementScreen(
    childId: String,
    navigateBack: () -> Unit,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var showLogMedicationDialog by remember { mutableStateOf(false) }
    var selectedMedicationId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(key1 = childId) {
        viewModel.loadMedicationsForChild(childId)
    }
    
    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MedicationEvent.MedicationAdded -> {
                    showAddMedicationDialog = false
                }
                is MedicationEvent.MedicationLogged -> {
                    showLogMedicationDialog = false
                }
                is MedicationEvent.MedicationUpdated -> {
                    // Handle medication update if needed
                }
                is MedicationEvent.ShowMessage -> {

                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = "Medication Management",
                onBackClick = navigateBack,
                actions = {
                    if (uiState.isAdmin) {
                        IconButton(onClick = { showAddMedicationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Medication"
                            )
                        }
                    }
                }
            )
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
            } else if (uiState.medications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No medications scheduled",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (uiState.isAdmin) {
                        Button(
                            onClick = { showAddMedicationDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Medication")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Medications",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Filter by priority option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter by priority:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        val filterOptions = listOf(
                            "All",
                            MedicationPriority.CRITICAL.name,
                            MedicationPriority.HIGH.name,
                            MedicationPriority.MEDIUM.name,
                            MedicationPriority.LOW.name
                        )
                        
                        var selectedFilter by remember { mutableStateOf(filterOptions[0]) }
                        
                        filterOptions.forEach { option ->
                            val isSelected = selectedFilter == option
                            val color = when (option) {
                                MedicationPriority.CRITICAL.name -> MaterialTheme.colorScheme.error
                                MedicationPriority.HIGH.name -> MaterialTheme.colorScheme.tertiary
                                MedicationPriority.MEDIUM.name -> MaterialTheme.colorScheme.secondary
                                MedicationPriority.LOW.name -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFilter = option
                                    viewModel.filterMedications(
                                        if (option == "All") null else MedicationPriority.valueOf(option)
                                    )
                                },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.2f),
                                    selectedLabelColor = color
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.filteredMedications) { medication ->
                            Card(
                                onClick = {
                                    selectedMedicationId = medication.id
                                    showLogMedicationDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = medication.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        
                                        val priorityColor = when (medication.priority) {
                                            MedicationPriority.CRITICAL.name -> MaterialTheme.colorScheme.error
                                            MedicationPriority.HIGH.name -> MaterialTheme.colorScheme.tertiary
                                            MedicationPriority.MEDIUM.name -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                        
                                        Badge(
                                            containerColor = priorityColor
                                        ) {
                                            Text(
                                                text = medication.priority,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = "Dosage: ${medication.dosage}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    
                                    medication.schedule.split(", ").forEach { scheduleItem ->
                                        Text(
                                            text = "• $scheduleItem",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    
                                    if (medication.instructions != null) {
                                        Text(
                                            text = "Instructions: ${medication.instructions}",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val startDate = LocalDate.parse(
                                            medication.startDate, 
                                            DateTimeFormatter.ISO_LOCAL_DATE
                                        ).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                        
                                        val endDateText = medication.endDate?.let {
                                            val endDate = LocalDate.parse(
                                                it,
                                                DateTimeFormatter.ISO_LOCAL_DATE
                                            ).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                            "End: $endDate"
                                        } ?: "No end date"
                                        
                                        Text(
                                            text = "Start: $startDate",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        
                                        Text(
                                            text = endDateText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (uiState.isAdmin) {
                                            TextButton(
                                                onClick = { viewModel.onEditMedication(medication.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Edit")
                                            }
                                        }
                                        
                                        OutlinedButton(
                                            onClick = {
                                                selectedMedicationId = medication.id
                                                showLogMedicationDialog = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Log",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Log")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Medication Dialog
    if (showAddMedicationDialog) {
        AddMedicationDialog(
            onDismiss = { showAddMedicationDialog = false },
            onAddMedication = { medicationData ->
                viewModel.addMedication(
                    childId = childId,
                    name = medicationData.name,
                    dosage = medicationData.dosage,
                    instructions = medicationData.instructions,
                    startDate = medicationData.startDate,
                    endDate = medicationData.endDate,
                    frequency = medicationData.frequency,
                    priority = medicationData.priority,
                    times = medicationData.times,
                    days = medicationData.days
                )
            }
        )
    }
    
    // Log Medication Dialog
    if (showLogMedicationDialog && selectedMedicationId != null) {
        LogMedicationDialog(
            medication = uiState.filteredMedications.find { it.id == selectedMedicationId },
            onDismiss = { showLogMedicationDialog = false },
            onLogMedication = { status, notes ->
                selectedMedicationId?.let {
                    viewModel.logMedicationAdministration(
                        medicationId = it,
                        status = status,
                        notes = notes
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var showEndDatePicker by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf("daily") }
    var priority by remember { mutableStateOf(MedicationPriority.MEDIUM.name) }
    var times by remember { mutableStateOf(listOf("09:00")) }
    var days by remember { mutableStateOf("1,2,3,4,5,6,7") } // Default to every day
    
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
                    text = "Add Medication",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
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
                        .padding(bottom = 8.dp)
                )
                
                // Date Range
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = LocalDate.parse(startDate).format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        onValueChange = { },
                        label = { Text("Start Date") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { /* Show date picker */ }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = endDate?.let { 
                            LocalDate.parse(it).format(DateTimeFormatter.ofPattern("MMM d, yyyy")) 
                        } ?: "No End Date",
                        onValueChange = { },
                        label = { Text("End Date") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
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
                    
                    val priorities = MedicationPriority.entries.map { it.name }
                    val priorityColors = mapOf(
                        MedicationPriority.CRITICAL.name to MaterialTheme.colorScheme.error,
                        MedicationPriority.HIGH.name to MaterialTheme.colorScheme.tertiary,
                        MedicationPriority.MEDIUM.name to MaterialTheme.colorScheme.secondary,
                        MedicationPriority.LOW.name to MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { prio ->
                            FilterChip(
                                selected = priority == prio,
                                onClick = { priority = prio },
                                label = { Text(prio) },
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
                                label = { Text("Time") },
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
                        "Monday" to "1",
                        "Tuesday" to "2",
                        "Wednesday" to "3",
                        "Thursday" to "4",
                        "Friday" to "5",
                        "Saturday" to "6",
                        "Sunday" to "7"
                    )
                    
                    val selectedDays = days.split(",").toSet()
                    
                    Column {
                        daysOfWeek.forEach { (dayName, dayValue) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedDays.contains(dayValue),
                                        onClick = {
                                            days = if (selectedDays.contains(dayValue)) {
                                                selectedDays.filter { it != dayValue }
                                            } else {
                                                selectedDays + dayValue
                                            }.sorted().joinToString(",")
                                        },
                                        role = Role.Checkbox
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedDays.contains(dayValue),
                                    onCheckedChange = null
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
                            if (name.isNotBlank() && dosage.isNotBlank()) {
                                onAddMedication(
                                    MedicationFormData(
                                        name = name,
                                        dosage = dosage,
                                        instructions = instructions.ifBlank { null },
                                        startDate = startDate,
                                        endDate = endDate,
                                        frequency = frequency,
                                        priority = MedicationPriority.valueOf(priority),
                                        times = times,
                                        days = days
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank() && dosage.isNotBlank() && days.isNotBlank() && times.isNotEmpty(),
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
fun LogMedicationDialog(
    medication: MedicationUi?,
    onDismiss: () -> Unit,
    onLogMedication: (String, String?) -> Unit
) {
    if (medication == null) {
        return
    }
    
    var selectedStatus by remember { mutableStateOf("COMPLETED") }
    var notes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Log Medication",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Medication: ${medication.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Dosage: ${medication.dosage}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // Status Selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val statuses = listOf("COMPLETED", "MISSED", "RESCHEDULED")
                    val statusLabels = mapOf(
                        "COMPLETED" to "Administered",
                        "MISSED" to "Missed",
                        "RESCHEDULED" to "Rescheduled"
                    )
                    
                    Column {
                        statuses.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedStatus == status,
                                        onClick = { selectedStatus = status },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStatus == status,
                                    onClick = null
                                )
                                Text(
                                    text = statusLabels[status] ?: status,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
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
                            onLogMedication(
                                selectedStatus,
                                notes.ifBlank { null }
                            )
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Log")
                    }
                }
            }
        }
    }
}

// Form data for adding medication
data class MedicationFormData(
    val name: String,
    val dosage: String,
    val instructions: String?,
    val startDate: String,
    val endDate: String?,
    val frequency: String,
    val priority: MedicationPriority,
    val times: List<String>,
    val days: String
)