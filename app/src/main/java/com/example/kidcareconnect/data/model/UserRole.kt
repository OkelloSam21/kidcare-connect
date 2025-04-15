package com.example.kidcareconnect.data.model

enum class UserRole {
    ADMIN,
    CARETAKER
}

enum class MedicationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class MealType {
    BREAKFAST,
    LUNCH,
    SNACK,
    DINNER
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    MISSED,
    RESCHEDULED
}