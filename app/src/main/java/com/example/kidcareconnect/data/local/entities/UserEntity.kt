package com.example.kidcareconnect.data.local.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kidcareconnect.data.model.UserRole
import java.time.LocalDate
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "role") val role: UserRole,
    @ColumnInfo(name = "profile_picture_url") val profilePictureUrl: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "children")
data class Child constructor(
    @PrimaryKey val childId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "date_of_birth") val dateOfBirth: LocalDate,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "blood_group") val bloodGroup: String? = null,
    @ColumnInfo(name = "profile_picture_url") val profilePictureUrl: String? = null,
    @ColumnInfo(name = "emergency_contact") val emergencyContact: String,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)