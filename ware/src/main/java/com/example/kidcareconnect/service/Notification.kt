//package com.example.kidcareconnect.presentation.service
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.app.TaskStackBuilder
//import androidx.lifecycle.LifecycleService
//import androidx.lifecycle.lifecycleScope
//import com.example.kidcareconnect.R
//import com.example.kidcareconnect.presentation.MainActivity
//import com.example.kidcareconnect.presentation.data.model.PendingTaskUi
//import com.example.kidcareconnect.presentation.data.repository.TaskRepository
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class NotificationService : LifecycleService() {
//
//    @Inject
//    lateinit var taskRepository: TaskRepository
//
//    private val notificationManager by lazy {
//        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    }
//
//    // Notification channels
//    companion object {
//        private const val CHANNEL_ID_MEDICATION = "medication_channel"
//        private const val CHANNEL_ID_MEAL = "meal_channel"
//        private const val CHANNEL_ID_HEALTH = "health_channel"
//        private const val CHANNEL_ID_CRITICAL = "critical_channel"
//
//        private const val NOTIFICATION_ID_ONGOING = 1
//
//        private const val CHECK_INTERVAL_MS = 60000L // 1 minute
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        createNotificationChannels()
//        startForeground(NOTIFICATION_ID_ONGOING, createForegroundNotification())
//
//        // Schedule task checks
//        lifecycleScope.launch {
//            while (isActive) {
//                checkUpcomingTasks()
//                delay(CHECK_INTERVAL_MS)
//            }
//        }
//    }
//
//    private fun createNotificationChannels() {
//        // Medication channel
//        val medicationChannel = NotificationChannel(
//            CHANNEL_ID_MEDICATION,
//            "Medication Reminders",
//            NotificationManager.IMPORTANCE_DEFAULT
//        ).apply {
//            description = "Reminders for medication administration"
//            enableVibration(true)
//        }
//
//        // Meal channel
//        val mealChannel = NotificationChannel(
//            CHANNEL_ID_MEAL,
//            "Meal Reminders",
//            NotificationManager.IMPORTANCE_DEFAULT
//        ).apply {
//            description = "Reminders for meal times"
//            enableVibration(true)
//        }
//
//        // Health check channel
//        val healthChannel = NotificationChannel(
//            CHANNEL_ID_HEALTH,
//            "Health Check Reminders",
//            NotificationManager.IMPORTANCE_DEFAULT
//        ).apply {
//            description = "Reminders for health checks"
//            enableVibration(true)
//        }
//
//        // Critical channel
//        val criticalChannel = NotificationChannel(
//            CHANNEL_ID_CRITICAL,
//            "Critical Alerts",
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "Critical alerts requiring immediate attention"
//            enableVibration(true)
//            vibrationPattern = longArrayOf(0, 500, 250, 500)
//            lightColor = Color.RED
//        }
//
//        // Register the channels
//        notificationManager.createNotificationChannels(
//            listOf(medicationChannel, mealChannel, healthChannel, criticalChannel)
//        )
//    }
//
//    private fun createForegroundNotification(): Notification {
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            Intent(this, MainActivity::class.java),
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        return NotificationCompat.Builder(this, CHANNEL_ID_HEALTH)
//            .setSmallIcon(R.drawable.splash_icon)
//            .setContentTitle("Smart Child Care")
//            .setContentText("Monitoring children's health and care needs")
//            .setContentIntent(pendingIntent)
//            .setSilent(true)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//    }
//
//    private suspend fun checkUpcomingTasks() {
//        taskRepository.getPendingTasks().collectLatest { tasks ->
//            val currentTime = System.currentTimeMillis()
//
//            for (task in tasks) {
//                val timeUntilTask = task.time - currentTime
//
//                // Send notification 30 minutes before and when the task is due
//                if (timeUntilTask in 0..TimeUnit.MINUTES.toMillis(30)) {
//                    sendTaskNotification(task)
//                }
//            }
//        }
//    }
//
//    private fun sendTaskNotification(task: PendingTaskUi) {
//        val channelId = when (task.type) {
//            "medication" -> CHANNEL_ID_MEDICATION
//            "meal" -> CHANNEL_ID_MEAL
//            "health" -> CHANNEL_ID_HEALTH
//            else -> CHANNEL_ID_HEALTH
//        }
//
//        val intent = Intent(this, MainActivity::class.java).apply {
//            putExtra("TASK_ID", task.id)
//            putExtra("CHILD_ID", task.childId)
//            putExtra("TASK_TYPE", task.type)
//        }
//
//        val pendingIntent = TaskStackBuilder.create(this).run {
//            addNextIntentWithParentStack(intent)
//            getPendingIntent(task.id.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        }
//
//        val iconRes = when (task.type) {
//            "medication" -> android.R.drawable.ic_dialog_alert
//            "meal" -> android.R.drawable.ic_menu_recent_history
//            "health" -> android.R.drawable.ic_menu_info_details
//            else -> android.R.drawable.ic_dialog_info
//        }
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(iconRes)
//            .setContentTitle(task.title)
//            .setContentText("For ${task.childName} at ${formatTime(task.time)}")
//            .setPriority(if (task.priority >= 2) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
//            .setCategory(NotificationCompat.CATEGORY_REMINDER)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .setVibrate(if (task.priority >= 2) longArrayOf(0, 500, 250, 500) else longArrayOf(0, 250))
//            .build()
//
//        with(NotificationManagerCompat.from(this)) {
//            notify(task.id.hashCode(), notification)
//        }
//    }
//
//    private fun formatTime(timeMillis: String): String {
//        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
//        return formatter.format(java.util.Date(timeMillis))
//    }
//}
