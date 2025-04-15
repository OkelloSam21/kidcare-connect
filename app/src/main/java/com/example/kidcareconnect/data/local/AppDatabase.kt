package com.example.kidcareconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kidcareconnect.data.local.dao.*
import com.example.kidcareconnect.data.local.entities.CaretakerChildAssignment
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.local.entities.DietaryPlan
import com.example.kidcareconnect.data.local.entities.HealthLog
import com.example.kidcareconnect.data.local.entities.MealLog
import com.example.kidcareconnect.data.local.entities.MealSchedule
import com.example.kidcareconnect.data.local.entities.Medication
import com.example.kidcareconnect.data.local.entities.MedicationLog
import com.example.kidcareconnect.data.local.entities.MedicationSchedule
import com.example.kidcareconnect.data.local.entities.Notification
import com.example.kidcareconnect.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        Child::class,
        CaretakerChildAssignment::class,
        Medication::class,
        MedicationSchedule::class,
        MedicationLog::class,
        DietaryPlan::class,
        MealSchedule::class,
        MealLog::class,
        HealthLog::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun childDao(): ChildDao
    abstract fun medicationDao(): MedicationDao
    abstract fun dietaryPlanDao(): DietaryPlanDao
    abstract fun healthLogDao(): HealthLogDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_childcare_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}