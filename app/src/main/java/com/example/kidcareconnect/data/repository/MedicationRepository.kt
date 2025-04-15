package com.example.kidcareconnect.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.dao.MedicationDao
import com.example.kidcareconnect.data.local.entities.Medication
import com.example.kidcareconnect.data.local.entities.MedicationLog
import com.example.kidcareconnect.data.local.entities.MedicationSchedule
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao
) {
    fun getMedicationsForChild(childId: String) = medicationDao.getMedicationsForChild(childId)
    
    fun getSchedulesForMedication(medicationId: String) = medicationDao.getSchedulesForMedication(medicationId)
    
    fun getMedicationLogsForChild(childId: String) = medicationDao.getMedicationLogsForChild(childId)
    
    fun getPendingMedicationsForChild(childId: String) = 
        medicationDao.getMedicationLogsByStatus(childId, TaskStatus.PENDING)
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createMedication(
        childId: String,
        name: String,
        dosage: String,
        instructions: String?,
        startDate: LocalDate,
        endDate: LocalDate?,
        frequency: String,
        priority: MedicationPriority,
        createdBy: String
    ): Medication {
        val medication = Medication(
            medicationId = UUID.randomUUID().toString(),
            childId = childId,
            name = name,
            dosage = dosage,
            instructions = instructions,
            startDate = startDate,
            endDate = endDate,
            frequency = frequency,
            priority = priority,
            createdBy = createdBy
        )
        medicationDao.insertMedication(medication)
        return medication
    }
    
    suspend fun createMedicationSchedule(
        medicationId: String,
        time: String,
        days: String
    ): MedicationSchedule {
        val schedule = MedicationSchedule(
            scheduleId = UUID.randomUUID().toString(),
            medicationId = medicationId,
            time = time,
            days = days
        )
        medicationDao.insertMedicationSchedule(schedule)
        return schedule
    }
    
    suspend fun logMedicationAdministration(
        medicationId: String,
        scheduledTime: LocalDateTime,
        administeredTime: LocalDateTime?,
        administeredBy: String,
        status: TaskStatus,
        notes: String?
    ): MedicationLog {
        val log = MedicationLog(
            logId = UUID.randomUUID().toString(),
            medicationId = medicationId,
            scheduledTime = scheduledTime,
            administeredTime = administeredTime,
            administeredBy = administeredBy,
            status = status,
            notes = notes
        )
        medicationDao.insertMedicationLog(log)
        return log
    }
    
    suspend fun updateMedicationLog(log: MedicationLog) = medicationDao.updateMedicationLog(log)
}