package com.example.kidcareconnect.data.local.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(
    tableName = "health_logs",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["childId"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["logged_by"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class HealthLog(
    @PrimaryKey val logId: String,
    @ColumnInfo(name = "childId") val childId: String,
    @ColumnInfo(name = "temperature") val temperature: Float? = null,
    @ColumnInfo(name = "heart_rate") val heartRate: Int? = null,
    @ColumnInfo(name = "symptoms") val symptoms: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "logged_by") val loggedBy: String? = null,
    @ColumnInfo(name = "logged_at") val loggedAt: LocalDateTime = LocalDateTime.now()
)