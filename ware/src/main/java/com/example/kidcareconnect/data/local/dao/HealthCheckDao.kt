package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidcareconnect.data.local.entity.HealthCheck
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthCheckDao {
    @Query("SELECT * FROM health_checks WHERE childId = :childId")
    fun getHealthChecksForChild(childId: String): Flow<List<HealthCheck>>

    @Query("SELECT * FROM health_checks WHERE id = :checkId")
    fun getHealthCheck(checkId: String): Flow<HealthCheck?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checks: List<HealthCheck>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(check: HealthCheck)

    @Query("UPDATE health_checks SET lastChecked = :timestamp WHERE id = :checkId")
    suspend fun updateLastChecked(checkId: String, timestamp: Long)
}
