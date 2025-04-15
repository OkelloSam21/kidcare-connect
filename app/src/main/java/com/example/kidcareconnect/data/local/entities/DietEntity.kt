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
    tableName = "dietary_plans",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["childId"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DietaryPlan(
    @PrimaryKey val planId: String,
    @ColumnInfo(name = "childId") val childId: String,
    @ColumnInfo(name = "allergies") val allergies: String? = null,
    @ColumnInfo(name = "restrictions") val restrictions: String? = null,
    @ColumnInfo(name = "preferences") val preferences: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)