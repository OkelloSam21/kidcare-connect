package com.example.kidcareconnect.data.repository

import com.example.kidcareconnect.data.local.dao.MealDao
import com.example.kidcareconnect.data.local.entity.Meal
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val mealDao: MealDao
) {
    fun getMealsForChild(childId: String): Flow<List<Meal>> {
        return mealDao.getMealsForChild(childId)
    }

    fun getMeal(mealId: String): Flow<Meal?> {
        return mealDao.getMeal(mealId)
    }

    suspend fun logMealServed(mealId: String) {
        mealDao.updateLastServed(mealId, System.currentTimeMillis())
    }

    suspend fun addMeal(meal: Meal) {
        mealDao.insert(meal)
    }

    // For demo/preview purposes
    companion object {
        fun getPreviewData(childId: String): List<Meal> {
            return listOf(
                Meal(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    mealType = "lunch",
                    time = System.currentTimeMillis() + 60 * 60 * 1000, // 1 hour from now
                    dietaryRestrictions = "Vegetarian",
                    allergies = "Nuts, Dairy",
                    lastServed = null
                ),
                Meal(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    mealType = "snack",
                    time = System.currentTimeMillis() + 3 * 60 * 60 * 1000, // 3 hours from now
                    dietaryRestrictions = null,
                    allergies = "Nuts",
                    lastServed = null
                )
            )
        }
    }
}
