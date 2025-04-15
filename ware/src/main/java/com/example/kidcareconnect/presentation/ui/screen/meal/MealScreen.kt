package com.example.kidcareconnect.presentation.ui.screen.meal

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
import com.example.kidcareconnect.data.local.entity.Meal
import com.example.kidcareconnect.presentation.ui.components.AppIcons
import com.example.kidcareconnect.presentation.ui.components.ChildCareIcons
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MealScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    viewModel: MealViewModel = hiltViewModel()
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
                is MealScreenEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            if (uiState.meals.isNotEmpty()) {
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
                uiState.meals.isEmpty() -> {
                    // No meals
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Meals",
                            style = MaterialTheme.typography.title2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.childName} doesn't have any scheduled meals",
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
                    // Meal list
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
                                    text = "${uiState.childName}'s Meals",
                                    style = MaterialTheme.typography.title3
                                )
                            }
                        }

                        // Meals list
                        items(uiState.meals) { meal ->
                            MealItem(
                                meal = meal,
                                onServed = { viewModel.markMealServed(meal.id) },
                                onNotServed = { viewModel.markMealNotServed(meal.id) },
                                onPartiallyServed = { viewModel.markMealPartiallyServed(meal.id) }
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
fun MealItem(
    meal: Meal,
    onServed: () -> Unit,
    onNotServed: () -> Unit,
    onPartiallyServed: () -> Unit
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
            // Meal info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChildCareIcons.MealIcon(
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatMealType(meal.mealType),
                        style = MaterialTheme.typography.body1
                    )

                    Text(
                        text = "At ${formatTime(meal.time)}",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )

                    // Show dietary restrictions and allergies if present
                    if (!meal.dietaryRestrictions.isNullOrEmpty() || !meal.allergies.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChildCareIcons.AllergensIcon(
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildRestrictionsText(meal),
                                style = MaterialTheme.typography.caption2,
                                color = MaterialTheme.colors.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Served button
                CompactChip(
                    onClick = onServed,
                    label = { Text("Served", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = AppIcons.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.primaryChipColors()
                )

                // Not Served button
                CompactChip(
                    onClick = onNotServed,
                    label = { Text("Not Served", maxLines = 1) },
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

                // Partially Served button (if screen space allows)
                CompactChip(
                    onClick = onPartiallyServed,
                    label = { Text("Partial", maxLines = 1) },
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

// Helper functions
private fun formatMealType(mealType: String): String {
    return when (mealType.lowercase()) {
        "breakfast" -> "Breakfast"
        "lunch" -> "Lunch"
        "dinner" -> "Dinner"
        "snack" -> "Snack"
        else -> mealType.capitalize()
    }
}

private fun buildRestrictionsText(meal: Meal): String {
    val restrictions = mutableListOf<String>()

    if (!meal.dietaryRestrictions.isNullOrEmpty()) {
        restrictions.add(meal.dietaryRestrictions)
    }

    if (!meal.allergies.isNullOrEmpty()) {
        restrictions.add("Allergies: ${meal.allergies}")
    }

    return restrictions.joinToString(" • ")
}

// Utility function for time formatting
private fun formatTime(timeMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timeMillis))
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
    }
}