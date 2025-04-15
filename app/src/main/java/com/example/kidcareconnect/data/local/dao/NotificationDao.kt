package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidcareconnect.data.local.entities.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY created_at DESC")
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND is_read = 0 ORDER BY priority DESC, created_at DESC")
    fun getUnreadNotificationsForUser(userId: String): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET is_read = 1 WHERE notificationId = :notificationId")
    suspend fun markNotificationAsRead(notificationId: String)
}
