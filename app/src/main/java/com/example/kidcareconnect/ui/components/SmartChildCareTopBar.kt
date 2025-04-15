package com.example.kidcareconnect.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kidcareconnect.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartChildCareTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { 
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions
    )
}

@Composable
fun SmartChildCareBottomBar(
    navController: NavController,
    currentRoute: String
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = {
                if (currentRoute != Screen.Dashboard.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
            label = { Text("Alerts") },
            selected = currentRoute == Screen.Notifications.route,
            onClick = {
                if (currentRoute != Screen.Notifications.route) {
                    navController.navigate(Screen.Notifications.route)
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                if (currentRoute != Screen.Settings.route) {
                    navController.navigate(Screen.Settings.route)
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    priority: Int = 0, // 0: normal, 1: high, 2: critical
    onClick: () -> Unit
) {
    val cardColor = when (priority) {
        2 -> MaterialTheme.colorScheme.errorContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open"
            )
        }
    }
}

@Composable
fun ChildListItem(
    name: String,
    age: String,
    profilePicture: @Composable () -> Unit,
    hasPendingTasks: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                profilePicture()
                
                if (hasPendingTasks) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("!")
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = age,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Profile"
            )
        }
    }
}