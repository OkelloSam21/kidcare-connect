
package com.example.kidcareconnect

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kidcareconnect.data.AuthManager
import com.example.kidcareconnect.data.repository.MockDataProvider
import com.example.kidcareconnect.data.repository.ThemeRepository
import com.example.kidcareconnect.data.repository.UserRepository
import com.example.kidcareconnect.ui.components.SmartChildCareBottomBar
import com.example.kidcareconnect.ui.navigation.Screen
import com.example.kidcareconnect.ui.navigation.SmartChildCareNavHost
import com.example.kidcareconnect.ui.screens.settings.rememberThemeController
import com.example.kidcareconnect.ui.theme.KidcareConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mockDataProvider: MockDataProvider

    @Inject
    lateinit var themeRepository: ThemeRepository

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var userRepository: UserRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize mock data in background
        CoroutineScope(Dispatchers.IO).launch {
            mockDataProvider.initializeMockData()

            // After initializing data, check for stored user authentication
            authManager.checkForStoredUser { userId ->
                userRepository.getUserById(userId)
            }
        }

        setContent {
            val isDarkTheme = rememberThemeController(themeRepository)
            KidcareConnectTheme(darkTheme = isDarkTheme) {
                SmartChildCareApp(mockDataProvider, authManager)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmartChildCareApp(mockDataProvider: MockDataProvider, authManager: AuthManager) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""

    // Observe login state for navigation
    val isLoggedIn = authManager.isLoggedIn.collectAsState().value

    // Effect to navigate to dashboard when logged in, or login when logged out
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Navigate to dashboard if login route showing
            if (currentRoute == Screen.Login.route) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        } else {
            // Navigate to login if not on login screen
            if (currentRoute != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (isLoggedIn && currentRoute != Screen.Login.route) {
                    SmartChildCareBottomBar(navController)
                }
            }
        ) { innerPadding ->
            SmartChildCareNavHost(
                navController = navController,
                mockDataProvider = mockDataProvider,
                modifier = Modifier.padding(innerPadding),
                startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
            )
        }
    }
}