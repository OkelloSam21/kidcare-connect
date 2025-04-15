package com.example.kidcareconnect.data.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.local.entities.DietaryPlan

data class ChildWithDietaryPlans(
    @Embedded
    val child: Child,

    @Relation(
        parentColumn = "childId",
        entityColumn = "childId",
        entity = DietaryPlan::class
    )
    val dietaryPlans: List<DietaryPlan> = emptyList()
)