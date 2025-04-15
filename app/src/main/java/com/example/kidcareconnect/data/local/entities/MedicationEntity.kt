package com.example.kidcareconnect.data.local.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["childId"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Medication(
    @PrimaryKey val medicationId: String,
    @ColumnInfo(name = "childId") val childId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "dosage") val dosage: String,
    @ColumnInfo(name = "instructions") val instructions: String? = null,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null,
    @ColumnInfo(name = "frequency") val frequency: String,
    @ColumnInfo(name = "priority") val priority: MedicationPriority = MedicationPriority.MEDIUM,
    @ColumnInfo(name = "created_by") val createdBy: String, // UserId of creator
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)

// Medication Schedule
@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["medicationId"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationSchedule(
    @PrimaryKey val scheduleId: String,
    @ColumnInfo(name = "medicationId") val medicationId: String,
    @ColumnInfo(name = "time") val time: String, // e.g., "09:00", "13:30"
    @ColumnInfo(name = "days") val days: String // e.g., "1,2,3,4,5" for weekdays
)

// Medication Log
@Entity(
    tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["medicationId"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["administered_by"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class MedicationLog(
    @PrimaryKey val logId: String,
    @ColumnInfo(name = "medicationId") val medicationId: String,
    @ColumnInfo(name = "scheduled_time") val scheduledTime: LocalDateTime,
    @ColumnInfo(name = "administered_time") val administeredTime: LocalDateTime? = null,
    @ColumnInfo(name = "administered_by") val administeredBy: String? = null,
    @ColumnInfo(name = "status") val status: TaskStatus = TaskStatus.PENDING,
    @ColumnInfo(name = "notes") val notes: String? = null
)