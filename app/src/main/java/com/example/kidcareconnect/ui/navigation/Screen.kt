package com.example.kidcareconnect.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kidcareconnect.data.repository.MockDataProvider
import com.example.kidcareconnect.ui.screens.child.ChildProfileScreen
import com.example.kidcareconnect.ui.screens.dashboard.DashboardScreen
import com.example.kidcareconnect.ui.screens.doctor.DoctorInputScreen
import com.example.kidcareconnect.ui.screens.login.LoginScreen
import com.example.kidcareconnect.ui.screens.meal.MealPlanningScreen
import com.example.kidcareconnect.ui.screens.medication.MedicationManagementScreen
import com.example.kidcareconnect.ui.screens.notifications.NotificationsScreen
import com.example.kidcareconnect.ui.screens.settings.SettingsScreen


// Navigation Routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ChildProfile : Screen("child/{childId}") {
        fun createRoute(childId: String) = "child/$childId"
    }
    object Medication : Screen("medication/{childId}") {
        fun createRoute(childId: String) = "medication/$childId"
    }
    object MealPlanning : Screen("meal/{childId}") {
        fun createRoute(childId: String) = "meal/$childId"
    }
    object Notifications : Screen("notifications")
    object DoctorInput : Screen("doctor/{childId}") {
        fun createRoute(childId: String) = "doctor/$childId"
    }
    object Settings : Screen("settings")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmartChildCareNavHost(
    navController: NavHostController,
    mockDataProvider: MockDataProvider,
    startDestination: String = Screen.Login.route
) {
    // Initialize mock data when the app starts
    LaunchedEffect(key1 = true) {
        mockDataProvider.initializeMockData()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navigateToDashboard = {
                navController.navigate(Screen.Dashboard.route)
            })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navigateToChildProfile = { childId ->
                    navController.navigate(Screen.ChildProfile.createRoute(childId))
                },
                navigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                navigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.ChildProfile.route,
            arguments = listOf(
                navArgument("childId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            Log.d("CHildProfile", "Child ID: $childId")
            ChildProfileScreen(
                childId = childId,
                navigateToMedication = {
                    navController.navigate(Screen.Medication.createRoute(childId))
                },
                navigateToMealPlanning = {
                    navController.navigate(Screen.MealPlanning.createRoute(childId))
                },
                navigateToDoctorInput = {
                    navController.navigate(Screen.DoctorInput.createRoute(childId))
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Medication.route,
            arguments = listOf(
                navArgument("childId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            MedicationManagementScreen(
                childId = childId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.MealPlanning.route,
            arguments = listOf(
                navArgument("childId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            MealPlanningScreen(
                childId = childId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                navigateToChildProfile = { childId ->
                    navController.navigate(Screen.ChildProfile.createRoute(childId))
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DoctorInput.route,
            arguments = listOf(
                navArgument("childId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            DoctorInputScreen(
                childId = childId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToLogin = {
                    navController.navigate(
                        route = Screen.Login.route,
                    ){
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}