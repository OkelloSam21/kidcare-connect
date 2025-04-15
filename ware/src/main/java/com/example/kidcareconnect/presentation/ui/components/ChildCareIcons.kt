package com.example.kidcareconnect.presentation.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

// Extension object to add custom icons to the Material Icons
object ChildCareIcons {
    // We'll use emoji-based icons for now since we can't define custom vector icons directly
    
    @Composable
    fun MedicationIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "💊",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun MealIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "🍽️",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun HealthIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "❤️",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun DiaperIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "👶",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun NapIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "😴",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun TemperatureIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "🌡️",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun AllergensIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.primary
    ) {
        Text(
            text = "⚠️",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }

    @Composable
    fun EmergencyIcon(
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.error
    ) {
        Text(
            text = "🚨",
            fontWeight = FontWeight.Bold,
            modifier = modifier
        )
    }
}

// Material icons with fallbacks to standard icons
object AppIcons {
    // We'll map these to standard Material icons
    val MedicationLiquid: ImageVector = Icons.Default.Notifications // Placeholder
    val Restaurant: ImageVector = Icons.Default.Notifications // Placeholder
    val Monitor: ImageVector = Icons.Default.Notifications // Placeholder
    val CheckCircle: ImageVector = Icons.Default.Check
    val Cancel: ImageVector = Icons.Default.Close
    val Star: ImageVector = Icons.Rounded.Star
}