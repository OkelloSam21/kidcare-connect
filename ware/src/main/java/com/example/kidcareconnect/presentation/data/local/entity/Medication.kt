package com.example.kidcareconnect.presentation.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Medication(
    @PrimaryKey
    val id: String,
    val childId: String,
    val name: String,
    val dosage: String,
    val time: Long,
    val frequency: String, // daily, twice_daily, etc.
    val instructions: String?,
    val isCritical: Boolean,
    val lastAdministered: Long?
)
