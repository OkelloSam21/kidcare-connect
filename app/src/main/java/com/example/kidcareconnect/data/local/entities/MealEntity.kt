package com.example.kidcareconnect.data.local.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.TaskStatus
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(
    tableName = "meal_schedules",
    foreignKeys = [
        ForeignKey(
            entity = DietaryPlan::class,
            parentColumns = ["planId"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MealSchedule(
    @PrimaryKey val scheduleId: String,
    @ColumnInfo(name = "planId") val planId: String,
    @ColumnInfo(name = "meal_type") val mealType: MealType,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "days") val days: String,
    @ColumnInfo(name = "menu") val menu: String? = null
)

// Meal Log
@Entity(
    tableName = "meal_logs",
    foreignKeys = [
        ForeignKey(
            entity = MealSchedule::class,
            parentColumns = ["scheduleId"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["served_by"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class MealLog(
    @PrimaryKey val logId: String,
    @ColumnInfo(name = "scheduleId") val scheduleId: String,
    @ColumnInfo(name = "scheduled_time") val scheduledTime: LocalDateTime,
    @ColumnInfo(name = "served_time") val servedTime: LocalDateTime? = null,
    @ColumnInfo(name = "served_by") val servedBy: String? = null,
    @ColumnInfo(name = "status") val status: TaskStatus = TaskStatus.PENDING,
    @ColumnInfo(name = "notes") val notes: String? = null
)