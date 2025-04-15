package com.example.kidcareconnect.data.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.local.entities.Medication

data class ChildWithMedications(
    @Embedded
    val child: Child,

    @Relation(
        parentColumn = "childId",
        entityColumn = "childId",
        entity = Medication::class
    )
    val medications: List<Medication> = emptyList()
)