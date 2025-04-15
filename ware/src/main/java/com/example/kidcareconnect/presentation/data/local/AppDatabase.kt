package com.example.kidcareconnect.presentation.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kidcareconnect.presentation.data.local.dao.ChildDao
import com.example.kidcareconnect.presentation.data.local.dao.HealthCheckDao
import com.example.kidcareconnect.presentation.data.local.dao.MealDao
import com.example.kidcareconnect.presentation.data.local.dao.MedicationDao
import com.example.kidcareconnect.presentation.data.local.dao.TaskDao
import com.example.kidcareconnect.presentation.data.local.entity.Child
import com.example.kidcareconnect.presentation.data.local.entity.HealthCheck
import com.example.kidcareconnect.presentation.data.local.entity.Meal
import com.example.kidcareconnect.presentation.data.local.entity.Medication
import com.example.kidcareconnect.presentation.data.local.entity.Task

@Database(
    entities = [
        Child::class,
        Task::class,
        Medication::class,
        Meal::class,
        HealthCheck::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun taskDao(): TaskDao
    abstract fun medicationDao(): MedicationDao
    abstract fun mealDao(): MealDao
    abstract fun healthCheckDao(): HealthCheckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kidcare_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}