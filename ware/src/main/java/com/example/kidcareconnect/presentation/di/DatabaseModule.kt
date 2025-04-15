package com.example.kidcareconnect.presentation.di

import android.content.Context
import com.example.kidcareconnect.presentation.data.local.AppDatabase
import com.example.kidcareconnect.presentation.data.local.dao.ChildDao
import com.example.kidcareconnect.presentation.data.local.dao.HealthCheckDao
import com.example.kidcareconnect.presentation.data.local.dao.MealDao
import com.example.kidcareconnect.presentation.data.local.dao.MedicationDao
import com.example.kidcareconnect.presentation.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    fun provideChildDao(database: AppDatabase): ChildDao {
        return database.childDao()
    }
    
    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    fun provideMedicationDao(database: AppDatabase): MedicationDao {
        return database.medicationDao()
    }
    
    @Provides
    fun provideMealDao(database: AppDatabase): MealDao {
        return database.mealDao()
    }
    
    @Provides
    fun provideHealthCheckDao(database: AppDatabase): HealthCheckDao {
        return database.healthCheckDao()
    }
}
