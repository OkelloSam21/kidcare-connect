package com.example.kidcareconnect.presentation.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun rememberSwipeDismissableNavController(): NavHostController {
    return androidx.wear.compose.navigation.rememberSwipeDismissableNavController()
}