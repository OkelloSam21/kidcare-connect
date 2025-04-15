package com.example.kidcareconnect.presentation.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import com.example.kidcareconnect.presentation.ui.components.ChildTaskItem
import com.example.kidcareconnect.presentation.ui.components.PendingTaskItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    navigateToMedication: (String) -> Unit,
    navigateToMeal: (String) -> Unit,
    navigateToHealth: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HomeScreenEvent.NavigateToMedication -> navigateToMedication(event.childId)
                is HomeScreenEvent.NavigateToMeal -> navigateToMeal(event.childId)
                is HomeScreenEvent.NavigateToHealth -> navigateToHealth(event.childId)
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.assignedChildren.isEmpty()) {
            // No children assigned
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Children",
                    style = MaterialTheme.typography.title2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You don't have any children assigned to you",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Show children and pending tasks
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 32.dp, // Leave space for the TimeText
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header for upcoming tasks
                if (uiState.pendingTasks.isNotEmpty()) {
                    item {
                        ListHeader {
                            Text(
                                text = "Upcoming Tasks",
                                style = MaterialTheme.typography.title3
                            )
                        }
                    }
                    
                    // Display pending tasks
                    items(uiState.pendingTasks) { task ->
                        PendingTaskItem(
                            task = task,
                            onClick = {
                                when (task.type) {
                                    "medication" -> viewModel.onMedicationTaskSelected(task)
                                    "meal" -> viewModel.onMealTaskSelected(task)
                                    "health" -> viewModel.onHealthTaskSelected(task)
                                }
                            }
                        )
                    }
                    
                    // Divider between tasks and children
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Header for children list
                item {
                    ListHeader {
                        Text(
                            text = "My Children",
                            style = MaterialTheme.typography.title3
                        )
                    }
                }
                
                // Display children
                items(uiState.assignedChildren) { child ->
                    ChildTaskItem(
                        child = child,
                        onMedicationClick = { viewModel.onMedicationSelected(child.id) },
                        onMealClick = { viewModel.onMealSelected(child.id) },
                        onHealthClick = { viewModel.onHealthSelected(child.id) }
                    )
                }
            }
        }
    }
}
