package com.example.kidcareconnect.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.TaskStatus
import com.example.kidcareconnect.data.model.UserRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Converters {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }

    @TypeConverter
    fun fromMedicationPriority(priority: MedicationPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toMedicationPriority(priority: String): MedicationPriority {
        return MedicationPriority.valueOf(priority)
    }

    @TypeConverter
    fun fromMealType(mealType: MealType): String {
        return mealType.name
    }

    @TypeConverter
    fun toMealType(mealType: String): MealType {
        return MealType.valueOf(mealType)
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus {
        return TaskStatus.valueOf(status)
    }
}