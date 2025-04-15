package com.example.kidcareconnect.data.local.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val notificationId: String,
    @ColumnInfo(name = "userId") val userId: String,
    @ColumnInfo(name = "childId") val childId: String? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "type") val type: String, // medication, meal, health, etc.
    @ColumnInfo(name = "priority") val priority: Int = 0, // 0: normal, 1: high, 2: critical
    @ColumnInfo(name = "action_id") val actionId: String? = null, // ID to related entity
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)