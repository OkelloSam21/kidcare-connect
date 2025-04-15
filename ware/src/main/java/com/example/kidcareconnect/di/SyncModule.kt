package com.example.kidcareconnect.di

import android.content.Context
import com.example.kidcareconnect.data.local.AppDatabase
import com.example.kidcareconnect.data.sync.DataSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideDataSyncMAnager(
        @ApplicationContext context: Context,
        database:AppDatabase
    ): DataSyncManager {
        return DataSyncManager(context, database)
    }
}