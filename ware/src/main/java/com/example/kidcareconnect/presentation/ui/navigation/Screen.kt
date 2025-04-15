package com.example.kidcareconnect.presentation.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Medication : Screen("medication/{childId}") {
        fun createRoute(childId: String) = "medication/$childId"
    }
    object Meal : Screen("meal/{childId}") {
        fun createRoute(childId: String) = "meal/$childId"
    }
    object Health : Screen("health/{childId}") {
        fun createRoute(childId: String) = "health/$childId"
    }
}