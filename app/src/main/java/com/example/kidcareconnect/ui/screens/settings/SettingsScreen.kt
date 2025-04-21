
package com.example.kidcareconnect.ui.screens.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidcareconnect.data.repository.ThemeRepository
import com.example.kidcareconnect.ui.components.SmartChildCareTopBar
import kotlinx.coroutines.flow.collectLatest

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
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel.event) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is SettingsEvent.NavigateToLogin -> navigateToLogin()
                is SettingsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ProfileUpdated -> showEditProfileDialog = false
            }
        }
    }

    Scaffold(
        topBar = {
            SmartChildCareTopBar(
                title = "Settings",
                onBackClick = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = uiState.userPhone,
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
                        onClick = { showEditProfileDialog = true },
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
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            name = uiState.userName,
            email = uiState.userEmail,
            phone = uiState.userPhone,
            onDismiss = { showEditProfileDialog = false },
            onSave = { profileData ->
                viewModel.updateProfile(profileData)
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.signOut()
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
}

@Composable
fun EditProfileDialog(
    name: String,
    email: String,
    phone: String,
    onDismiss: () -> Unit,
    onSave: (ProfileUpdateData) -> Unit
) {
    var editedName by remember { mutableStateOf(name) }
    var editedEmail by remember { mutableStateOf(email) }
    var editedPhone by remember { mutableStateOf(phone) }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    // Validation functions
    val validateName = { input: String ->
        input.isNotBlank() && input.length >= 3
    }

    val validateEmail = { input: String ->
        input.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    val validatePhone = { input: String ->
        input.isNotBlank() && input.length >= 5
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Name field
                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        nameError = !validateName(it)
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Name must be at least 3 characters")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email field
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = {
                        editedEmail = it
                        emailError = !validateEmail(it)
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError,
                    supportingText = {
                        if (emailError) {
                            Text("Please enter a valid email address")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone field
                OutlinedTextField(
                    value = editedPhone,
                    onValueChange = {
                        editedPhone = it
                        phoneError = !validatePhone(it)
                    },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = phoneError,
                    supportingText = {
                        if (phoneError) {
                            Text("Please enter a valid phone number")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (!nameError && !emailError && !phoneError) {
                                onSave(
                                    ProfileUpdateData(
                                        name = editedName,
                                        email = editedEmail,
                                        phone = editedPhone
                                    )
                                )
                            }
                        },
                        enabled = validateName(editedName) && validateEmail(editedEmail) && validatePhone(editedPhone)
                    ) {
                        Text("Save")
                    }
                }
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