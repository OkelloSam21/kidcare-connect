package com.example.kidcareconnect.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.dao.ChildDao
import com.example.kidcareconnect.data.local.entities.CaretakerChildAssignment
import com.example.kidcareconnect.data.local.entities.Child
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepository @Inject constructor(
    private val childDao: ChildDao
) {
    fun getAllChildren() = childDao.getAllChildren()
    
    fun getChildrenForCaretaker(caretakerId: String) = childDao.getChildrenForCaretaker(caretakerId)
    
    suspend fun getChildById(childId: String) = childDao.getChildById(childId)
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createChild(
        name: String,
        dateOfBirth: LocalDate,
        gender: String,
        bloodGroup: String? = null,
        profilePictureUrl: String? = null,
        emergencyContact: String,
        notes: String? = null
    ): Child {
        val child = Child(
            childId = UUID.randomUUID().toString(),
            name = name,
            dateOfBirth = dateOfBirth,
            gender = gender,
            bloodGroup = bloodGroup,
            profilePictureUrl = profilePictureUrl,
            emergencyContact = emergencyContact,
            notes = notes
        )
        childDao.insertChild(child)
        return child
    }
    
    suspend fun updateChild(child: Child) = childDao.updateChild(child)
    
    suspend fun deleteChild(child: Child) = childDao.deleteChild(child)
    
    suspend fun assignCaretakerToChild(caretakerId: String, childId: String) {
        val assignment = CaretakerChildAssignment(
            caretakerId = caretakerId,
            childId = childId
        )
        childDao.assignCaretaker(assignment)
    }
    
    suspend fun removeCaretakerAssignment(caretakerId: String, childId: String) = 
        childDao.removeCaretakerAssignment(caretakerId, childId)
    
    fun getChildWithMedications(childId: String) = childDao.getChildWithMedications(childId)
    
    fun getChildWithDietaryPlans(childId: String) = childDao.getChildWithDietaryPlans(childId)
    
    fun getChildWithHealthLogs(childId: String) = childDao.getChildWithHealthLogs(childId)
}