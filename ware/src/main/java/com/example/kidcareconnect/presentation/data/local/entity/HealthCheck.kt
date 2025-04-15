package com.example.kidcareconnect.presentation.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_checks",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HealthCheck(
    @PrimaryKey
    val id: String,
    val childId: String,
    val checkType: String, // temperature, diaper, nap, etc.
    val scheduledTime: Long,
    val notes: String?,
    val lastChecked: Long?
)