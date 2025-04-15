package com.example.kidcareconnect.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.kidcareconnect.presentation.ui.screen.health.HealthScreen
import com.example.kidcareconnect.presentation.ui.screen.home.HomeScreen
import com.example.kidcareconnect.presentation.ui.screen.meal.MealScreen
import com.example.kidcareconnect.presentation.ui.screen.medication.MedicationScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navigateToMedication = { childId ->
                    navController.navigate(Screen.Medication.createRoute(childId))
                },
                navigateToMeal = { childId ->
                    navController.navigate(Screen.Meal.createRoute(childId))
                },
                navigateToHealth = { childId ->
                    navController.navigate(Screen.Health.createRoute(childId))
                }
            )
        }

        composable(
            route = Screen.Medication.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            MedicationScreen(
                childId = childId,
                onNavigateBack = { navController.popBackStack()},
//                viewModel =
            )
        }

        composable(
            route = Screen.Meal.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            MealScreen(
                childId = childId,
                onNavigateBack = { navController.popBackStack() },
//                viewModel =
            )

        }

        composable(
            route = Screen.Health.route,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            HealthScreen(
                childId = childId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}


