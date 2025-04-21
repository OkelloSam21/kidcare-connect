package com.example.kidcareconnect.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kidcareconnect.data.model.MedicationPriority
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMedicationDialog(
    childId: String,
    onDismiss: () -> Unit,
    onAddMedication: (
        name: String,
        dosage: String,
        instructions: String?,
        startDate: LocalDate,
        endDate: LocalDate?,
        frequency: String,
        priority: MedicationPriority,
        time: String,
        days: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var frequency by remember { mutableStateOf("Daily") }
    var priority by remember { mutableStateOf(MedicationPriority.MEDIUM) }
    var time by remember { mutableStateOf("08:00") }
    var selectedDays by remember { mutableStateOf("1,2,3,4,5,6,7") }

    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Medication") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                // Start Date Picker
                OutlinedTextField(
                    value = startDate.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Start Date*") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select start date"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartDatePicker = true },
                    readOnly = true
                )
                
                // End Date Picker (Optional)
                OutlinedTextField(
                    value = endDate?.format(dateFormatter) ?: "",
                    onValueChange = { },
                    label = { Text("End Date (optional)") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select end date"
                        )
                    },
                    placeholder = { Text("No end date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndDatePicker = true },
                    readOnly = true
                )
                
                // Time Picker
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time*") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("HH:MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
                
                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { },
                        label = { Text("Frequency*") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = showStartDatePicker
                            )
                    )
                }
                
                // Priority Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    OutlinedTextField(
                        value = priority.name,
                        onValueChange = { },
                        label = { Text("Priority*") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = showEndDatePicker
                            )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank() && startDate != null) {
                        onAddMedication(
                            name,
                            dosage,
                            if (instructions.isBlank()) null else instructions,
                            startDate,
                            endDate,
                            frequency,
                            priority,
                            time,
                            selectedDays
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = startDatePickerState.selectedDateMillis?.let { millis ->
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}