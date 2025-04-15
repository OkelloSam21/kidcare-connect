package com.example.kidcareconnect.data.repository


import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.dao.UserDao
import com.example.kidcareconnect.data.local.entities.UserEntity
import com.example.kidcareconnect.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getAllUsers() = userDao.getAllUsers()

    fun getUsersByRole(role: UserRole) = userDao.getUsersByRole(role)

    suspend fun getUserById(userId: String) = userDao.getUserById(userId)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createUser(
        name: String,
        email: String,
        phone: String,
        role: UserRole,
        profilePictureUrl: String? = null
    ): UserEntity {
        val user = UserEntity(
            userId = UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = phone,
            role = role,
            profilePictureUrl = profilePictureUrl
        )
        userDao.insertUser(user)
        return user
    }

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
}