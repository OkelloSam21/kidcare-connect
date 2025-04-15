package com.example.kidcareconnect.ui.screens.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.model.UserRole
import com.example.kidcareconnect.ui.components.ChildListItem
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import com.example.kidcareconnect.ui.components.TaskCard
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    navigateToChildProfile: (String) -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showMyChildrenOnly = remember { mutableStateOf(true) } // Default to showing only assigned children
    val snackbarHostState = remember { SnackbarHostState() }

    // Update showMyChildrenOnly based on user role
    LaunchedEffect(uiState.currentUserRole) {
        showMyChildrenOnly.value = uiState.currentUserRole == UserRole.CARETAKER
    }

    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when(event) {
                is DashboardEvent.NavigateToChild -> {
                    navigateToChildProfile(event.childId)
                }
                is DashboardEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = "Dashboard",
                actions = {
                    IconButton(onClick = navigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationsCount > 0) {
                                    Badge { Text(text = uiState.unreadNotificationsCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }

                    IconButton(onClick = navigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Current User Info
            if (uiState.currentUser != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome, ${uiState.currentUser?.name}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Role: ${if (uiState.currentUserRole == UserRole.ADMIN) "Administrator" else "Caretaker"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // User role indicator
//                    Chip(
//                        onClick = { },
//                        colors = ChipDefaults.chipColors(
//                            containerColor = if (uiState.currentUserRole == UserRole.ADMIN)
//                                MaterialTheme.colorScheme.primary
//                            else
//                                MaterialTheme.colorScheme.secondary
//                        ),
//                        leadingIcon = {
//                            Icon(
//                                imageVector = if (uiState.currentUserRole == UserRole.ADMIN)
//                                    Icons.Default.AdminPanelSettings
//                                else
//                                    Icons.Default.HealthAndSafety,
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.onPrimary
//                            )
//                        }
//                    ) {
//                        Text(
//                            text = if (uiState.currentUserRole == UserRole.ADMIN) "Admin" else "Caretaker",
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                    }
                }
            }

            // Filter options (only for admin users)
            if (uiState.currentUserRole == UserRole.ADMIN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show all children",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = showMyChildrenOnly.value,
                        onCheckedChange = { showMyChildrenOnly.value = it }
                    )
                    Text(
                        text = "My children only",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Pending Tasks Section
                if (uiState.pendingTasks.isNotEmpty()) {
                    Text(
                        text = "Pending Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(uiState.pendingTasks) { task ->
                            TaskCard(
                                title = task.title,
                                description = task.description,
                                icon = {
                                    Icon(
                                        imageVector = when (task.type) {
                                            "medication" -> Icons.Default.Medication
                                            "meal" -> Icons.Default.Restaurant
                                            "health" -> Icons.Default.HealthAndSafety
                                            else -> Icons.Default.Assignment
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                priority = task.priority,
                                onClick = { viewModel.onTaskSelected(task) }
                            )
                        }
                    }
                } else {
                    //no pending task
                    Text(
                        text = "No pending tasks",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                }

                // Children Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Children",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Child count badge
                    val childrenToShow = when {
                        uiState.currentUserRole == UserRole.CARETAKER -> uiState.children
                        showMyChildrenOnly.value -> uiState.myAssignedChildren
                        else -> uiState.children
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${childrenToShow.size} children",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Determine which children to show based on role and filter
                val childrenToShow = when {
                    uiState.currentUserRole == UserRole.CARETAKER -> uiState.children // Caretakers always see just their assigned children
                    showMyChildrenOnly.value -> uiState.myAssignedChildren // Admin with filter on
                    else -> uiState.children // Admin with filter off
                }

                if (childrenToShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (uiState.currentUserRole == UserRole.ADMIN && !showMyChildrenOnly.value)
                                    "No children found in the system"
                                else
                                    "No children assigned to you",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            if (uiState.currentUserRole == UserRole.ADMIN) {
                                Button(
                                    onClick = { viewModel.onAddChildClicked() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Child")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(if (uiState.pendingTasks.isEmpty()) 1f else 0.6f)
                            .fillMaxWidth()
                    ) {
                        items(childrenToShow) { child ->
                            ChildListItem(
                                name = child.name,
                                age = "Age: ${viewModel.calculateAge(child.dateOfBirth)}",
                                profilePicture = {
                                    if (child.profilePictureUrl != null) {
                                        // Image loading would go here
                                        // For now, we'll use a placeholder
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = child.name.first().toString(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    } else {
                                        // Default avatar
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = child.name.first().toString(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                },
                                hasPendingTasks = viewModel.childHasPendingTasks(child.childId),
                                onClick = { viewModel.onChildSelected(child.childId) }
                            )
                        }

                        // Add Child button for Admin
                        if (uiState.currentUserRole == UserRole.ADMIN) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = { viewModel.onAddChildClicked() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Child")
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