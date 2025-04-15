package com.example.kidcareconnect.data.repository

import com.example.kidcareconnect.data.local.dao.MedicationDao
import com.example.kidcareconnect.data.local.entity.Medication
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao
) {
    fun getMedicationsForChild(childId: String): Flow<List<Medication>> {
        return medicationDao.getMedicationsForChild(childId)
    }

    fun getMedication(medicationId: String): Flow<Medication?> {
        return medicationDao.getMedication(medicationId)
    }

    suspend fun logMedicationAdministered(medicationId: String) {
        medicationDao.updateLastAdministered(medicationId, System.currentTimeMillis())
    }

    suspend fun addMedication(medication: Medication) {
        medicationDao.insert(medication)
    }

    // For demo/preview purposes
    companion object {
        fun getPreviewData(childId: String): List<Medication> {
            return listOf(
                Medication(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    name = "Tylenol",
                    dosage = "5ml",
                    time = System.currentTimeMillis() + 30 * 60 * 1000, // 30 minutes from now
                    frequency = "as_needed",
                    instructions = "Give for fever above 100.4°F",
                    isCritical = true,
                    lastAdministered = System.currentTimeMillis() - 12 * 60 * 60 * 1000 // 12 hours ago
                ),
                Medication(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    name = "Vitamin D",
                    dosage = "1 drop",
                    time = System.currentTimeMillis() + 2 * 60 * 60 * 1000, // 2 hours from now
                    frequency = "daily",
                    instructions = "Give with breakfast",
                    isCritical = false,
                    lastAdministered = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours ago
                )
            )
        }
    }
}
