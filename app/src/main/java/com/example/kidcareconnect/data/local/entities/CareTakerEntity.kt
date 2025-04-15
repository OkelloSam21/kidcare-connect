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
    tableName = "caretaker_child_assignments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["caretakerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Child::class,
            parentColumns = ["childId"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CaretakerChildAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "caretakerId") val caretakerId: String,
    @ColumnInfo(name = "childId") val childId: String,
    @ColumnInfo(name = "assigned_at") val assignedAt: LocalDateTime = LocalDateTime.now()
)