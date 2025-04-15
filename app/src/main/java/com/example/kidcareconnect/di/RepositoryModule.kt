package com.example.kidcareconnect.di

import com.example.kidcareconnect.data.local.dao.ChildDao
import com.example.kidcareconnect.data.local.dao.DietaryPlanDao
import com.example.kidcareconnect.data.local.dao.HealthLogDao
import com.example.kidcareconnect.data.local.dao.MedicationDao
import com.example.kidcareconnect.data.local.dao.NotificationDao
import com.example.kidcareconnect.data.local.dao.UserDao
import com.example.kidcareconnect.data.repository.ChildRepository
import com.example.kidcareconnect.data.repository.DietaryRepository
import com.example.kidcareconnect.data.repository.HealthRepository
import com.example.kidcareconnect.data.repository.MedicationRepository
import com.example.kidcareconnect.data.repository.NotificationRepository
import com.example.kidcareconnect.data.repository.UserRepository
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
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }
    
    @Provides
    @Singleton
    fun provideChildRepository(childDao: ChildDao): ChildRepository {
        return ChildRepository(childDao)
    }
    
    @Provides
    @Singleton
    fun provideMedicationRepository(medicationDao: MedicationDao): MedicationRepository {
        return MedicationRepository(medicationDao)
    }
    
    @Provides
    @Singleton
    fun provideDietaryRepository(dietaryPlanDao: DietaryPlanDao): DietaryRepository {
        return DietaryRepository(dietaryPlanDao)
    }
    
    @Provides
    @Singleton
    fun provideHealthRepository(healthLogDao: HealthLogDao): HealthRepository {
        return HealthRepository(healthLogDao)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(notificationDao: NotificationDao): NotificationRepository {
        return NotificationRepository(notificationDao)
    }
}