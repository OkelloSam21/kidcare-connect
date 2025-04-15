package com.example.kidcareconnect.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.dao.NotificationDao
import com.example.kidcareconnect.data.local.entities.Notification
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    fun getNotificationsForUser(userId: String) = notificationDao.getNotificationsForUser(userId)
    
    fun getUnreadNotificationsForUser(userId: String) = notificationDao.getUnreadNotificationsForUser(userId)
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createNotification(
        userId: String,
        childId: String?,
        title: String,
        message: String,
        type: String,
        priority: Int = 0,
        actionId: String?
    ): Notification {
        val notification = Notification(
            notificationId = UUID.randomUUID().toString(),
            userId = userId,
            childId = childId,
            title = title,
            message = message,
            type = type,
            priority = priority,
            actionId = actionId
        )
        notificationDao.insertNotification(notification)
        return notification
    }
    
    suspend fun markNotificationAsRead(notificationId: String) = 
        notificationDao.markNotificationAsRead(notificationId)
}