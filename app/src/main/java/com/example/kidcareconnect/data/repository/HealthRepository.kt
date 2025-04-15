package com.example.kidcareconnect.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.dao.HealthLogDao
import com.example.kidcareconnect.data.local.entities.HealthLog
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(
    private val healthLogDao: HealthLogDao
) {
    fun getHealthLogsForChild(childId: String) = healthLogDao.getHealthLogsForChild(childId)
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createHealthLog(
        childId: String,
        temperature: Float?,
        heartRate: Int?,
        symptoms: String?,
        notes: String?,
        loggedBy: String
    ): HealthLog {
        val log = HealthLog(
            logId = UUID.randomUUID().toString(),
            childId = childId,
            temperature = temperature,
            heartRate = heartRate,
            symptoms = symptoms,
            notes = notes,
            loggedBy = loggedBy
        )
        healthLogDao.insertHealthLog(log)
        return log
    }
}