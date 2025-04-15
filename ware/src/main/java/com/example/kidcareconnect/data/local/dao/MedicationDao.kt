package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidcareconnect.data.local.entity.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE childId = :childId")
    fun getMedicationsForChild(childId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :medicationId")
    fun getMedication(medicationId: String): Flow<Medication?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Query("UPDATE medications SET lastAdministered = :timestamp WHERE id = :medicationId")
    suspend fun updateLastAdministered(medicationId: String, timestamp: Long)
}
