package com.example.kidcareconnect.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onAddChild: (
        name: String, dateOfBirth: LocalDate, gender: String,
        bloodGroup: String, emergencyContact: String, notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    var gender by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dateError by remember { mutableStateOf(false) }

// Initialize with non-null LocalDate
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Format for display
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd/yyyy") }
    val dateDisplayValue = selectedDate.format(dateFormatter)

    // Initialize date picker with current selection
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Child") },
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
                    label = { Text("Child Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                    OutlinedTextField(
                        value = dateDisplayValue ?: "",
                        onValueChange = { },
                        label = { Text("DOB") },
                        placeholder = { Text("MM/DD/YYYY") },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(dateDisplayValue) {
                                awaitEachGesture {
                                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                                    // in the Initial pass to observe events before the text field consumes them
                                    // in the Main pass.
                                    awaitFirstDown(pass = PointerEventPass.Initial)
                                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                    if (upEvent != null) {
                                        showDatePicker = true
                                    }
                                }
                            }
                    )

                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group") },
                        singleLine = true
                    )

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && !dateError && dateOfBirth != null) {
                        onAddChild(
                            name,
                            selectedDate,
                            gender,
                            bloodGroup,
                            emergencyContact,
                            notes
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && !dateError
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


    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Correctly convert milliseconds to LocalDate
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}



