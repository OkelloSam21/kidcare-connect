package com.example.kidcareconnect.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.example.kidcareconnect.presentation.theme.KidcareConnectTheme
import com.example.kidcareconnect.presentation.ui.navigation.AppNavHost
import com.example.kidcareconnect.presentation.ui.navigation.rememberSwipeDismissableNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            KidcareConnectTheme {
                SmartChildWearApp()
            }
        }
    }
}

@Composable
fun SmartChildWearApp() {
    val navController = rememberSwipeDismissableNavController()

    Scaffold(
        timeText = { TimeText() },
        modifier = Modifier.fillMaxSize()
    ) {
        AppNavHost(
            navController = navController,
            startDestination = "home"
        )
    }
}