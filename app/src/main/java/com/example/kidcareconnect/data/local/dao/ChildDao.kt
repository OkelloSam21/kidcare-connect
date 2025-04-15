package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.kidcareconnect.data.local.entities.CaretakerChildAssignment
import com.example.kidcareconnect.data.local.entities.Child
import com.example.kidcareconnect.data.local.relationship.ChildWithDietaryPlans
import com.example.kidcareconnect.data.local.relationship.ChildWithHealthLogs
import com.example.kidcareconnect.data.local.relationship.ChildWithMedications
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    @Query("SELECT * FROM children")
    fun getAllChildren(): Flow<List<Child>>

    @Query("SELECT * FROM children WHERE childId = :childId")
    suspend fun getChildById(childId: String): Child?

    @Query("""
        SELECT c.* FROM children c
        INNER JOIN caretaker_child_assignments a ON c.childId = a.childId
        WHERE a.caretakerId = :caretakerId
    """)
    fun getChildrenForCaretaker(caretakerId: String): Flow<List<Child>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: Child)

    @Update
    suspend fun updateChild(child: Child)

    @Delete
    suspend fun deleteChild(child: Child)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignCaretaker(assignment: CaretakerChildAssignment)

    @Query("DELETE FROM caretaker_child_assignments WHERE caretakerId = :caretakerId AND childId = :childId")
    suspend fun removeCaretakerAssignment(caretakerId: String, childId: String)
    
    @Transaction
    @Query("SELECT * FROM children WHERE childId = :childId")
    fun getChildWithMedications(childId: String): Flow<List<ChildWithMedications>>
    
    @Transaction
    @Query("SELECT * FROM children WHERE childId = :childId")
    fun getChildWithDietaryPlans(childId: String): Flow<List<ChildWithDietaryPlans>>
    
    @Transaction
    @Query("SELECT * FROM children WHERE childId = :childId")
    fun getChildWithHealthLogs(childId: String): Flow<List<ChildWithHealthLogs>>
}