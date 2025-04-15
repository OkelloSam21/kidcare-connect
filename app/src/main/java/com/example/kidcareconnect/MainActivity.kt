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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kidcareconnect.data.repository.MockDataProvider
import com.example.kidcareconnect.data.repository.ThemeRepository
import com.example.kidcareconnect.ui.components.SmartChildCareBottomBar
import com.example.kidcareconnect.ui.navigation.Screen
import com.example.kidcareconnect.ui.navigation.SmartChildCareNavHost
import com.example.kidcareconnect.ui.screens.settings.rememberThemeController
import com.example.kidcareconnect.ui.theme.KidcareConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mockDataProvider: MockDataProvider

    @Inject
    lateinit var themeRepository: ThemeRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme = rememberThemeController(themeRepository)
            KidcareConnectTheme(darkTheme = isDarkTheme) {
                SmartChildCareApp(mockDataProvider)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmartChildCareApp(mockDataProvider: MockDataProvider) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        color = MaterialTheme.colorScheme.background
    ) {
        SmartChildCareNavHost(
            navController = navController,
            mockDataProvider = mockDataProvider
        )
    }

}