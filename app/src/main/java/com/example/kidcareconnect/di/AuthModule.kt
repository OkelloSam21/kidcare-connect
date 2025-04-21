package com.example.kidcareconnect.di

import android.content.Context
import com.example.kidcareconnect.data.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun providesAuthManager(@ApplicationContext context: Context): AuthManager = AuthManager(context)
}