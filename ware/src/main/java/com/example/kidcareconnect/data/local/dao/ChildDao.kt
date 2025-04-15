package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kidcareconnect.data.local.entity.Child
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    @Query("SELECT * FROM children")
    fun getAllAssigned(): Flow<List<Child>>

    @Query("SELECT * FROM children WHERE id = :childId")
    fun getById(childId: String): Flow<Child?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(children: List<Child>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(child: Child)

    @Update
    suspend fun update(child: Child)

    @Query("UPDATE children SET hasPendingTasks = :hasPendingTasks WHERE id = :childId")
    suspend fun updatePendingTaskStatus(childId: String, hasPendingTasks: Boolean)

    @Query("SELECT COUNT(*) FROM children")
    suspend fun getCount(): Int
}