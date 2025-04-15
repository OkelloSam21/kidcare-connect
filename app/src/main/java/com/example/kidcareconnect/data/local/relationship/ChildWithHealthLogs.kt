package com.example.kidcareconnect.data.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.local.entities.HealthLog

data class ChildWithHealthLogs(
    @Embedded
    val child: Child,

    @Relation(
        parentColumn = "childId",
        entityColumn = "childId",
        entity = HealthLog::class
    )
    val healthLogs: List<HealthLog> = emptyList()
)