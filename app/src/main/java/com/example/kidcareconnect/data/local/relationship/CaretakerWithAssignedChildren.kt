package com.example.kidcareconnect.data.local.relationship

import androidx.room.Relation
import com.example.kidcareconnect.data.local.entities.CaretakerChildAssignment

data class CaretakerWithAssignedChildren(
    @Relation(
        parentColumn = "userId",
        entityColumn = "caretakerId",
        entity = CaretakerChildAssignment::class
    )
    val assignments: List<CaretakerChildAssignment>
)