package com.example.kidcareconnect.presentation.ui.screen.medication

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.kidcareconnect.data.local.entity.Medication
import com.example.kidcareconnect.presentation.ui.components.AppIcons
import com.example.kidcareconnect.presentation.ui.components.ChildCareIcons
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MedicationScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    // Initialize the view model with the child ID
    LaunchedEffect(key1 = childId) {
        viewModel.initialize(childId)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation events
    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MedicationScreenEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            if (uiState.medications.isNotEmpty()) {
                PositionIndicator(
                    scalingLazyListState = uiState.scrollState
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.childName.isEmpty() -> {
                    // Error state - child not found
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Child Not Found",
                            style = MaterialTheme.typography.title2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unable to load child information",
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.primaryButtonColors()
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                uiState.medications.isEmpty() -> {
                    // No medications
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Medications",
                            style = MaterialTheme.typography.title2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.childName} doesn't have any scheduled medications",
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.primaryButtonColors()
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    // Medication list
                    ScalingLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 32.dp,
                            bottom = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = uiState.scrollState
                    ) {
                        // Header with child name
                        item {
                            ListHeader {
                                Text(
                                    text = "${uiState.childName}'s Medications",
                                    style = MaterialTheme.typography.title3
                                )
                            }
                        }

                        // Medications list
                        items(uiState.medications) { medication ->
                            MedicationItem(
                                medication = medication,
                                onAdministered = { viewModel.markMedicationAdministered(medication.id) },
                                onMissed = { viewModel.markMedicationMissed(medication.id) },
                                onReschedule = { viewModel.rescheduleMedication(medication.id) }
                            )
                        }

                        // Back button at bottom
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.secondaryButtonColors()
                            ) {
                                Text("Back to Home")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationItem(
    medication: Medication,
    onAdministered: () -> Unit,
    onMissed: () -> Unit,
    onReschedule: () -> Unit
) {
    Card(
        onClick = { /* Expand details */ },
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = MaterialTheme.colors.surface,
            endBackgroundColor = MaterialTheme.colors.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Medication info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChildCareIcons.MedicationIcon(
                    modifier = Modifier.size(24.dp),
                    tint = if (medication.isCritical) MaterialTheme.colors.error else MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.body1
                    )

                    Text(
                        text = "${medication.dosage} at ${formatTime(medication.time)}",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )
                }

                // Critical indicator
                if (medication.isCritical) {
                    Chip(
                        colors = ChipDefaults.chipColors(
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.2f)
                        ),
                        label = {
                            Text(
                                text = "Critical",
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption2
                            )
                        },
                        onClick = { /* Show info dialog */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Administered button
                CompactChip(
                    onClick = onAdministered,
                    label = { Text("Given", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = AppIcons.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.primaryChipColors()
                )

                // Missed button
                CompactChip(
                    onClick = onMissed,
                    label = { Text("Missed", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = AppIcons.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = MaterialTheme.colors.error
                    )
                )

                // Reschedule button if screen space allows
                CompactChip(
                    onClick = onReschedule,
                    label = { Text("Reschedule", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = AppIcons.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}

// Utility function for time formatting
private fun formatTime(timeMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timeMillis))
}