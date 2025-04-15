package com.example.kidcareconnect.di

import android.content.Context
import com.example.kidcareconnect.data.local.AppDatabase
import com.example.kidcareconnect.data.local.dao.ChildDao
import com.example.kidcareconnect.data.local.dao.DietaryPlanDao
import com.example.kidcareconnect.data.local.dao.HealthLogDao
import com.example.kidcareconnect.data.local.dao.MedicationDao
import com.example.kidcareconnect.data.local.dao.NotificationDao
import com.example.kidcareconnect.data.local.dao.UserDao
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
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideChildDao(appDatabase: AppDatabase): ChildDao {
        return appDatabase.childDao()
    }

    @Provides
    fun provideMedicationDao(appDatabase: AppDatabase): MedicationDao {
        return appDatabase.medicationDao()
    }

    @Provides
    fun provideDietaryPlanDao(appDatabase: AppDatabase): DietaryPlanDao {
        return appDatabase.dietaryPlanDao()
    }

    @Provides
    fun provideHealthLogDao(appDatabase: AppDatabase): HealthLogDao {
        return appDatabase.healthLogDao()
    }

    @Provides
    fun provideNotificationDao(appDatabase: AppDatabase): NotificationDao {
        return appDatabase.notificationDao()
    }
}