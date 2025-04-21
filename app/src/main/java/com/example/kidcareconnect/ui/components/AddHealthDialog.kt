package com.example.kidcareconnect.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kidcareconnect.ui.screens.doctor.HealthLogFormData


/**
 * Dialog for adding a new health log entry
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHealthLogDialog(
    onDismiss: () -> Unit,
    onAddHealthLog: (HealthLogFormData) -> Unit
) {
    var temperature by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Validation states
    var temperatureError by remember { mutableStateOf(false) }
    var heartRateError by remember { mutableStateOf(false) }

    // Form is valid if at least one field has content and all filled fields are valid
    val isFormValid = (temperature.isNotEmpty() || heartRate.isNotEmpty() ||
            symptoms.isNotEmpty() || notes.isNotEmpty()) &&
            !temperatureError && !heartRateError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Health Log") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Temperature
                OutlinedTextField(
                    value = temperature,
                    onValueChange = {
                        temperature = it
                        temperatureError = it.isNotEmpty() && it.toFloatOrNull() == null
                    },
                    label = { Text("Temperature (°F)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = temperatureError,
                    supportingText = {
                        if (temperatureError) {
                            Text("Please enter a valid temperature")
                        }
                    }
                )

                // Heart Rate
                OutlinedTextField(
                    value = heartRate,
                    onValueChange = {
                        heartRate = it
                        heartRateError = it.isNotEmpty() && it.toIntOrNull() == null
                    },
                    label = { Text("Heart Rate (bpm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = heartRateError,
                    supportingText = {
                        if (heartRateError) {
                            Text("Please enter a valid heart rate")
                        }
                    }
                )

                // Symptoms
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Notes
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
                    if (isFormValid) {
                        onAddHealthLog(
                            HealthLogFormData(
                                temperature = temperature.toFloatOrNull(),
                                heartRate = heartRate.toIntOrNull(),
                                symptoms = if (symptoms.isBlank()) null else symptoms,
                                notes = if (notes.isBlank()) null else notes
                            )
                        )
                    }
                },
                enabled = isFormValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
