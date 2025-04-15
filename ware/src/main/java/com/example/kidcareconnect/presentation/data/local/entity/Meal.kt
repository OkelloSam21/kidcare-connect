package com.example.kidcareconnect.presentation.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Meal(
    @PrimaryKey
    val id: String,
    val childId: String,
    val mealType: String, // breakfast, lunch, dinner, snack
    val time: Long,
    val dietaryRestrictions: String?,
    val allergies: String?,
    val lastServed: Long?
)