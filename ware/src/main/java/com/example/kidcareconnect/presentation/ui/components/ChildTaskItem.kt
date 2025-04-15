package com.example.kidcareconnect.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Badge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.kidcareconnect.presentation.data.model.ChildUi

@Composable
fun ChildTaskItem(
    child: ChildUi,
    onMedicationClick: () -> Unit,
    onMealClick: () -> Unit,
    onHealthClick: () -> Unit
) {
    Card(
        onClick = { /* Expand/collapse the card */ },
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = MaterialTheme.colors.surface,
            endBackgroundColor = MaterialTheme.colors.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Child info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar - first letter of name in a circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.7f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = child.name.first().toString(),
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.body1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = child.age,
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )
                }
                
                // Badge for pending tasks
                if (child.hasPendingTasks) {
                    Badge(
                        containerColor = MaterialTheme.colors.error
                    ) {
                        Text("!")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Medication button
                CompactChip(
                    onClick = onMedicationClick,
                    label = { Text("Meds", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.primaryChipColors()
                )
                
                // Meal button
                CompactChip(
                    onClick = onMealClick,
                    label = { Text("Meal", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
                
                // Health button
                CompactChip(
                    onClick = onHealthClick,
                    label = { Text("Health", maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Monitor,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}