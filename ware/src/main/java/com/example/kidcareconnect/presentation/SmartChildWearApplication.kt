package com.example.kidcareconnect.presentation

import android.app.Application
import com.example.kidcareconnect.data.sync.DataSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartChildWearApplication: Application() {

    @Inject
    lateinit var dataSyncManager: DataSyncManager

    override fun onCreate() {
        super.onCreate()

        dataSyncManager.initialize()
    }

    override fun onTerminate() {
        super.onTerminate()

        dataSyncManager.cleanup()
    }
}