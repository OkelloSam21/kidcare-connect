package com.example.kidcareconnect.presentation.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey
    val id: String,
    val childId: String,
    val childName: String,
    val title: String,
    val description: String?,
    val time: Long,
    val type: String, // medication, meal, health
    val status: String, // pending, completed, missed
    val priority: Int // 0: normal, 1: high, 2: critical
) {
    val formattedTime: String
        get() = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time))
}
