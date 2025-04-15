package com.example.kidcareconnect.ui.screens.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.repository.ThemeRepository
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    navigateToLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect (Unit){
        viewModel.event.collect { event ->
            when(event) {
                else -> navigateToLogin()
            }
        }
    }

    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = "Settings",
                onBackClick = navigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Profile Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = uiState.userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = if (uiState.isAdmin) "Administrator" else "Caretaker",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { /* Edit profile */ },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
            
            // Notifications Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = {
                                viewModel.toggleNotifications(it)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sound",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = uiState.soundEnabled,
                            onCheckedChange = {
                                viewModel.toggleSound(it)
                            },
                            enabled = uiState.notificationsEnabled
                        )
                    }
                }
            }
            
            // Appearance Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val themeOptions = listOf("system", "light", "dark")
                    val themeLabels = mapOf(
                        "system" to "System Default",
                        "light" to "Light",
                        "dark" to "Dark"
                    )
                    
                    Column {
                        themeOptions.forEach { theme ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.theme == theme,
                                    onClick = { viewModel.setTheme(theme) }
                                )
                                
                                Text(
                                    text = themeLabels[theme] ?: theme,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirm Logout") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            viewModel.signOut()
                            navigateToLogin()
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

            
            // About Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Smart Child Care",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Smart Child Care helps childcare centers manage medications, meals, and health monitoring for children.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Sign Out Button
            Button(
                onClick = {
                    showLogoutDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}

@Composable
fun rememberThemeController(themeRepository: ThemeRepository): Boolean {
    val themePreference = themeRepository.themeFlow.collectAsState(initial = "system")

    return when (themePreference.value) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
}

//@Composable
//fun ThemeSelectionSection(
//    currentTheme: String,
//    onThemeSelected: (String) -> Unit
//) {
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Text(
//            text = "Theme",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        val options = listOf("light", "dark", "system")
//        val labels = listOf("Light Theme", "Dark Theme", "System Default")
//
//        options.forEachIndexed { index, theme ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .selectable(
//                        selected = currentTheme == theme,
//                        onClick = { onThemeSelected(theme) }
//                    )
//                    .padding(vertical = 12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                RadioButton(
//                    selected = currentTheme == theme,
//                    onClick = { onThemeSelected(theme) }
//                )
//                Text(
//                    text = labels[index],
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(start = 16.dp)
//                )
//            }
//        }
//    }
//}