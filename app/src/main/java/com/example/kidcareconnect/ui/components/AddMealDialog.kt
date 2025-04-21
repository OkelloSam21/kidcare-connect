package com.example.kidcareconnect.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kidcareconnect.data.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMealDialog(
    childId: String,
    onDismiss: () -> Unit,
    onAddMeal: (
        allergies: String?,
        restrictions: String?,
        preferences: String?,
        notes: String?,
        mealType: MealType,
        time: String,
        days: String,
        menu: String?
    ) -> Unit
) {
    var allergies by remember { mutableStateOf("") }
    var restrictions by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var time by remember { mutableStateOf("08:00") }
    var days by remember { mutableStateOf("1,2,3,4,5,6,7") }
    var menu by remember { mutableStateOf("") }
    
    var expandedMealType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Meal Plan") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dietary Information Section
                Text(
                    text = "Dietary Information",
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = restrictions,
                    onValueChange = { restrictions = it },
                    label = { Text("Restrictions") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = preferences,
                    onValueChange = { preferences = it },
                    label = { Text("Preferences") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Schedule Section
                Text(
                    text = "Meal Schedule",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Meal Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedMealType,
                    onExpandedChange = { expandedMealType = it },
                ) {
                    OutlinedTextField(
                        value = mealType.name.capitalize(),
                        onValueChange = { },
                        label = { Text("Meal Type*") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = expandedMealType
                            )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedMealType,
                        onDismissRequest = { expandedMealType = false }
                    ) {
                        MealType.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.capitalize()) },
                                onClick = {
                                    mealType = option
                                    expandedMealType = false
                                }
                            )
                        }
                    }
                }
                
                // Time Input
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time*") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("HH:MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = menu,
                    onValueChange = { menu = it },
                    label = { Text("Menu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
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
                    onAddMeal(
                        if (allergies.isBlank()) null else allergies,
                        if (restrictions.isBlank()) null else restrictions,
                        if (preferences.isBlank()) null else preferences,
                        if (notes.isBlank()) null else notes,
                        mealType,
                        time,
                        days,
                        if (menu.isBlank()) null else menu
                    )
                    onDismiss()
                }
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
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}