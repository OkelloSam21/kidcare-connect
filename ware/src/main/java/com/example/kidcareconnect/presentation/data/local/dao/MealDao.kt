package com.example.kidcareconnect.presentation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidcareconnect.presentation.data.local.entity.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE childId = :childId")
    fun getMealsForChild(childId: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    fun getMeal(mealId: String): Flow<Meal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<Meal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal)

    @Query("UPDATE meals SET lastServed = :timestamp WHERE id = :mealId")
    suspend fun updateLastServed(mealId: String, timestamp: Long)
}