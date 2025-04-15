package com.example.kidcareconnect.presentation.di

import com.example.kidcareconnect.presentation.data.local.dao.ChildDao
import com.example.kidcareconnect.presentation.data.local.dao.HealthCheckDao
import com.example.kidcareconnect.presentation.data.local.dao.MealDao
import com.example.kidcareconnect.presentation.data.local.dao.MedicationDao
import com.example.kidcareconnect.presentation.data.local.dao.TaskDao
import com.example.kidcareconnect.presentation.data.repository.ChildRepository
import com.example.kidcareconnect.presentation.data.repository.HealthCheckRepository
import com.example.kidcareconnect.presentation.data.repository.MealRepository
import com.example.kidcareconnect.presentation.data.repository.MedicationRepository
import com.example.kidcareconnect.presentation.data.repository.TaskRepository
import com.google.android.gms.wearable.DataClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideChildRepository(
        childDao: ChildDao,
        dataClient: DataClient
    ): ChildRepository {
        return ChildRepository(childDao, dataClient)
    }
    
    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepository(taskDao)
    }
    
    @Provides
    @Singleton
    fun provideMedicationRepository(medicationDao: MedicationDao): MedicationRepository {
        return MedicationRepository(medicationDao)
    }
    
    @Provides
    @Singleton
    fun provideMealRepository(mealDao: MealDao): MealRepository {
        return MealRepository(mealDao)
    }
    
    @Provides
    @Singleton
    fun provideHealthCheckRepository(healthCheckDao: HealthCheckDao): HealthCheckRepository {
        return HealthCheckRepository(healthCheckDao)
    }
}