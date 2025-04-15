package com.example.kidcareconnect.presentation.ui.screen.health

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.kidcareconnect.presentation.data.local.entity.HealthCheck
import com.example.kidcareconnect.presentation.ui.components.AppIcons
import com.example.kidcareconnect.presentation.ui.components.ChildCareIcons
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HealthScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    viewModel: HealthViewModel = hiltViewModel()
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
                is HealthScreenEvent.NavigateBack -> onNavigateBack()
                // Other events would be handled here, like showing dialogs
                else -> { /* handle other events */ }
            }
        }
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            if (uiState.healthChecks.isNotEmpty()) {
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
                uiState.healthChecks.isEmpty() && !uiState.hasVitalData -> {
                    // No health checks or vital data
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Health Data",
                            style = MaterialTheme.typography.title2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.childName} doesn't have any scheduled health checks or records",
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
                    // Health checks list and/or vital data
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
                                    text = "${uiState.childName}'s Health",
                                    style = MaterialTheme.typography.title3
                                )
                            }
                        }

                        // Current status section if available
                        if (uiState.hasVitalData) {
                            item {
                                VitalStatsCard(
                                    temperature = uiState.temperature,
                                    lastDiaper = uiState.lastDiaper,
                                    onLogTemperature = { viewModel.onLogTemperature() },
                                    onLogDiaper = { viewModel.onLogDiaper() }
                                )
                            }
                        }

                        // Health checks section header (only if there are checks)
                        if (uiState.healthChecks.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Scheduled Checks",
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                            }

                            // Health checks list
                            items(uiState.healthChecks) { check ->
                                HealthCheckItem(
                                    healthCheck = check,
                                    onComplete = { viewModel.markHealthCheckCompleted(check.id) },
                                    onAddNote = { viewModel.onAddNote(check.id) }
                                )
                            }
                        }

                        // Quick actions section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Quick Log",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )

                            QuickActionsRow(
                                onLogTemperature = { viewModel.onLogTemperature() },
                                onLogDiaper = { viewModel.onLogDiaper() },
                                onLogNap = { viewModel.onLogNap() }
                            )
                        }

                        // Back button at bottom
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
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
fun VitalStatsCard(
    temperature: Float?,
    lastDiaper: Long?,
    onLogTemperature: () -> Unit,
    onLogDiaper: () -> Unit
) {
    Card(
        onClick = { /* No action */ },
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
                .padding(12.dp)
        ) {
            Text(
                text = "Current Status",
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChildCareIcons.TemperatureIcon(
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Temperature",
                        style = MaterialTheme.typography.caption1
                    )

                    Text(
                        text = if (temperature != null) {
                            String.format("%.1f°F", temperature)
                        } else {
                            "Not recorded"
                        },
                        style = MaterialTheme.typography.body2,
                        color = if (temperature != null && temperature > 100.4f)
                            MaterialTheme.colors.error else MaterialTheme.colors.onSurface
                    )
                }

                CompactChip(
                    onClick = onLogTemperature,
                    label = {
                        Text("Update", maxLines = 1, style = MaterialTheme.typography.caption2)
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last diaper row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChildCareIcons.DiaperIcon(
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Last Diaper",
                        style = MaterialTheme.typography.caption1
                    )

                    Text(
                        text = if (lastDiaper != null) {
                            formatTimeSince(lastDiaper)
                        } else {
                            "Not recorded"
                        },
                        style = MaterialTheme.typography.body2
                    )
                }

                CompactChip(
                    onClick = onLogDiaper,
                    label = {
                        Text("Update", maxLines = 1, style = MaterialTheme.typography.caption2)
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}

@Composable
fun HealthCheckItem(
    healthCheck: HealthCheck,
    onComplete: () -> Unit,
    onAddNote: () -> Unit
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
            // Health check info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (healthCheck.checkType) {
                    "temperature" -> ChildCareIcons.TemperatureIcon(modifier = Modifier.size(24.dp))
                    "diaper" -> ChildCareIcons.DiaperIcon(modifier = Modifier.size(24.dp))
                    "nap" -> ChildCareIcons.NapIcon(modifier = Modifier.size(24.dp))
                    else -> ChildCareIcons.HealthIcon(modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatCheckType(healthCheck.checkType),
                        style = MaterialTheme.typography.body1
                    )

                    Text(
                        text = "At ${formatTime(healthCheck.scheduledTime)}",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )

                    // Show notes if present
                    if (!healthCheck.notes.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = healthCheck.notes,
                            style = MaterialTheme.typography.caption2,
                            color = MaterialTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Complete button
                CompactChip(
                    onClick = onComplete,
                    label = { Text("Complete", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = AppIcons.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.primaryChipColors()
                )

                // Add Note button
                CompactChip(
                    onClick = onAddNote,
                    label = { Text("Add Note", maxLines = 1) },
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

@Composable
fun QuickActionsRow(
    onLogTemperature: () -> Unit,
    onLogDiaper: () -> Unit,
    onLogNap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Temperature button
        CompactChip(
            onClick = onLogTemperature,
            label = {
                Text("Temp", maxLines = 1)
            },
            icon = {
                ChildCareIcons.TemperatureIcon(
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = ChipDefaults.primaryChipColors()
        )

        // Diaper button
        CompactChip(
            onClick = onLogDiaper,
            label = {
                Text("Diaper", maxLines = 1)
            },
            icon = {
                ChildCareIcons.DiaperIcon(
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = ChipDefaults.primaryChipColors()
        )

        // Nap button
        CompactChip(
            onClick = onLogNap,
            label = {
                Text("Nap", maxLines = 1)
            },
            icon = {
                ChildCareIcons.NapIcon(
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = ChipDefaults.primaryChipColors()
        )
    }
}

// Helper functions
private fun formatCheckType(checkType: String): String {
    return when (checkType.lowercase()) {
        "temperature" -> "Temperature Check"
        "diaper" -> "Diaper Check"
        "nap" -> "Nap Schedule"
        else -> checkType.capitalize()
    }
}

// Utility function for time formatting
private fun formatTime(timeMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timeMillis))
}

// Utility function to format time since a timestamp
private fun formatTimeSince(timeMillis: Long): String {
    val current = System.currentTimeMillis()
    val diff = current - timeMillis

    val minutes = diff / (60 * 1000)
    val hours = minutes / 60

    return when {
        hours > 1 -> "$hours hours ago"
        hours == 1L -> "1 hour ago"
        minutes > 0 -> "$minutes minutes ago"
        else -> "Just now"
    }
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
    }
}