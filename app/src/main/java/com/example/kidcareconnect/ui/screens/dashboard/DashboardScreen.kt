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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navigateToChildProfile: (String) -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showMyChildrenOnly = remember { mutableStateOf(uiState.currentUserRole == UserRole.CARETAKER) }
    
    LaunchedEffect(key1 = true) {
        viewModel.loadChildren()
        viewModel.loadPendingTasks()
    }

    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collectLatest { event ->
            when(event) {
                is DashboardEvent.NavigateToChild -> {
                    navigateToChildProfile(event.childId)
                }

                is DashboardEvent.ShowMessage -> TODO()
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Current User Info
            if (uiState.currentUser != null) {
                Text(
                    text = "Welcome, ${uiState.currentUser?.name}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Filter options (for caretakers to see only their assigned children)
            if (uiState.currentUserRole == UserRole.ADMIN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
            
            // Pending Tasks Section
            if (uiState.pendingTasks.isNotEmpty()) {
                Text(
                    text = "Pending Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
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
            }
            
            // Children Section
            Text(
                text = "Children",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            if (uiState.children.isEmpty()) {
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
                            text = "No children found",
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
                    items(
                        if (showMyChildrenOnly.value) 
                            uiState.myAssignedChildren 
                        else 
                            uiState.children
                    ) { child ->
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