package com.example.kidcareconnect.data.local.dao

import androidx.room.*
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}