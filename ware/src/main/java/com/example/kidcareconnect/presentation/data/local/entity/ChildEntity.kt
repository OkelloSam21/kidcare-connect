package com.example.kidcareconnect.presentation.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class Child(
    @PrimaryKey
    val id: String,
    val name: String,
    val age: Int,
    val photoUrl: String?,
    val hasPendingTasks: Boolean,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)








