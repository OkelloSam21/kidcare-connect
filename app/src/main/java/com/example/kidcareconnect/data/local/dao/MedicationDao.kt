package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kidcareconnect.data.local.entities.Medication
import com.example.kidcareconnect.data.local.entities.MedicationLog
import com.example.kidcareconnect.data.local.entities.MedicationSchedule
import com.example.kidcareconnect.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE childId = :childId")
    fun getMedicationsForChild(childId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId")
    fun getSchedulesForMedication(medicationId: String): Flow<List<MedicationSchedule>>

    @Query("""
        SELECT ml.* FROM medication_logs ml
        INNER JOIN medications m ON ml.medicationId = m.medicationId
        WHERE m.childId = :childId
        ORDER BY ml.scheduled_time DESC
    """)
    fun getMedicationLogsForChild(childId: String): Flow<List<MedicationLog>>

    @Query("""
        SELECT ml.* FROM medication_logs ml
        INNER JOIN medications m ON ml.medicationId = m.medicationId
        WHERE m.childId = :childId AND ml.status = :status
        ORDER BY ml.scheduled_time
    """)
    fun getMedicationLogsByStatus(childId: String, status: TaskStatus): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationSchedule(schedule: MedicationSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog)

    @Update
    suspend fun updateMedicationLog(log: MedicationLog)
}