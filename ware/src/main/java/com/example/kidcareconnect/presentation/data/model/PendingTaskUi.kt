package com.example.kidcareconnect.presentation.data.model

data class PendingTaskUi(
    val id: String,
    val childId: String,
    val childName: String,
    val title: String,
    val time: String,
    val type: String, // medication, meal, health
    val priority: Int // 0: normal, 1: high, 2: critical
)