package com.example.kidcareconnect.ui.screens.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kidcareconnect.MainActivity
import com.example.kidcareconnect.R
import com.example.kidcareconnect.data.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    companion object {
        private const val CHANNEL_ID_MEDICATIONS = "medications_channel"
        private const val CHANNEL_ID_MEALS = "meals_channel"
        private const val CHANNEL_ID_HEALTH = "health_channel"
        private const val CHANNEL_ID_SYSTEM = "system_channel"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create medication channel
        val medicationChannel = NotificationChannel(
            CHANNEL_ID_MEDICATIONS,
            "Medications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Medication reminders and alerts"
        }

        // Create meal channel
        val mealChannel = NotificationChannel(
            CHANNEL_ID_MEALS,
            "Meals",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Meal schedules and reminders"
        }

        // Create health channel
        val healthChannel = NotificationChannel(
            CHANNEL_ID_HEALTH,
            "Health",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Health alerts and updates"
        }

        // Create system channel
        val systemChannel = NotificationChannel(
            CHANNEL_ID_SYSTEM,
            "System",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "System notifications"
        }

        notificationManager.createNotificationChannels(
            listOf(medicationChannel, mealChannel, healthChannel, systemChannel)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        type: String,
        priority: Int,
        childId: String? = null
    ) {
        val channelId = when (type.lowercase()) {
            "medication" -> CHANNEL_ID_MEDICATIONS
            "meal" -> CHANNEL_ID_MEALS
            "health" -> CHANNEL_ID_HEALTH
            else -> CHANNEL_ID_SYSTEM
        }

        // Create a pending intent that will open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("childId", childId)
            putExtra("notificationType", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use appropriate icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                when (priority) {
                    2 -> NotificationCompat.PRIORITY_HIGH
                    1 -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_LOW
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    // Create a notification for medication
    @RequiresApi(Build.VERSION_CODES.O)
    fun createMedicationNotification(
        userId: String,
        childId: String,
        childName: String,
        medicationName: String,
        dosage: String,
        time: LocalDateTime,
        medicationId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Save notification to database
            val notification = notificationRepository.createNotification(
                userId = userId,
                childId = childId,
                title = "Medication Reminder",
                message = "$childName's $medicationName ($dosage) is due at ${time.toLocalTime()}",
                type = "medication",
                priority = 2, // High priority for medications
                actionId = medicationId
            )

            // Show system notification
            showNotification(
                notificationId = notification.notificationId.hashCode(),
                title = "Medication Reminder",
                message = "$childName's $medicationName ($dosage) is due at ${time.toLocalTime()}",
                type = "medication",
                priority = 2,
                childId = childId
            )
        }
    }

    // Create a notification for meal
    @RequiresApi(Build.VERSION_CODES.O)
    fun createMealNotification(
        userId: String,
        childId: String,
        childName: String,
        mealType: String,
        time: LocalDateTime,
        scheduleId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Save notification to database
            val notification = notificationRepository.createNotification(
                userId = userId,
                childId = childId,
                title = "Meal Time",
                message = "$childName's $mealType is scheduled for ${time.toLocalTime()}",
                type = "meal",
                priority = 1, // Medium priority for meals
                actionId = scheduleId
            )

            // Show system notification
            showNotification(
                notificationId = notification.notificationId.hashCode(),
                title = "Meal Time",
                message = "$childName's $mealType is scheduled for ${time.toLocalTime()}",
                type = "meal",
                priority = 1,
                childId = childId
            )
        }
    }

    // Create a notification for health alert
    @RequiresApi(Build.VERSION_CODES.O)
    fun createHealthAlertNotification(
        userId: String,
        childId: String,
        childName: String,
        alert: String,
        details: String,
        healthLogId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Save notification to database
            val notification = notificationRepository.createNotification(
                userId = userId,
                childId = childId,
                title = "Health Alert: $childName",
                message = "$alert: $details",
                type = "health",
                priority = 2, // High priority for health alerts
                actionId = healthLogId
            )

            // Show system notification
            showNotification(
                notificationId = notification.notificationId.hashCode(),
                title = "Health Alert: $childName",
                message = "$alert: $details",
                type = "health",
                priority = 2,
                childId = childId
            )
        }
    }
}