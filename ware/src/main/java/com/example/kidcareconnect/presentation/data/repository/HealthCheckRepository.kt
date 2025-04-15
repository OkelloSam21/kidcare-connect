package com.example.kidcareconnect.presentation.data.repository

import com.example.kidcareconnect.presentation.data.local.dao.HealthCheckDao
import com.example.kidcareconnect.presentation.data.local.entity.HealthCheck
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthCheckRepository @Inject constructor(
    private val healthCheckDao: HealthCheckDao
) {
    fun getHealthChecksForChild(childId: String): Flow<List<HealthCheck>> {
        return healthCheckDao.getHealthChecksForChild(childId)
    }

    fun getHealthCheck(checkId: String): Flow<HealthCheck?> {
        return healthCheckDao.getHealthCheck(checkId)
    }

    suspend fun logHealthCheckCompleted(checkId: String) {
        healthCheckDao.updateLastChecked(checkId, System.currentTimeMillis())
    }

    suspend fun addHealthCheck(healthCheck: HealthCheck) {
        healthCheckDao.insert(healthCheck)
    }

    // For demo/preview purposes
    companion object {
        fun getPreviewData(childId: String): List<HealthCheck> {
            return listOf(
                HealthCheck(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    checkType = "temperature",
                    scheduledTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000, // 2 hours from now
                    notes = "Check temperature due to mild fever earlier",
                    lastChecked = System.currentTimeMillis() - 4 * 60 * 60 * 1000 // 4 hours ago
                ),
                HealthCheck(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    checkType = "diaper",
                    scheduledTime = System.currentTimeMillis() + 30 * 60 * 1000, // 30 minutes from now
                    notes = null,
                    lastChecked = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // 2 hours ago
                )
            )
        }
    }
}