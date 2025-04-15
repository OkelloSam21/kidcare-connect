package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidcareconnect.data.local.entities.HealthLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthLogDao {
    @Query("SELECT * FROM health_logs WHERE childId = :childId ORDER BY logged_at DESC")
    fun getHealthLogsForChild(childId: String): Flow<List<HealthLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthLog(log: HealthLog)
}