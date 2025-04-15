package com.example.kidcareconnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kidcareconnect.data.local.entities.DietaryPlan
import com.example.kidcareconnect.data.local.entities.MealLog
import com.example.kidcareconnect.data.local.entities.MealSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface DietaryPlanDao {
    @Query("SELECT * FROM dietary_plans WHERE childId = :childId")
    fun getDietaryPlanForChild(childId: String): Flow<DietaryPlan?>

    @Query("SELECT * FROM meal_schedules WHERE planId = :planId")
    fun getMealSchedulesForPlan(planId: String): Flow<List<MealSchedule>>

    @Query("""
        SELECT ml.* FROM meal_logs ml
        INNER JOIN meal_schedules ms ON ml.scheduleId = ms.scheduleId
        INNER JOIN dietary_plans dp ON ms.planId = dp.planId
        WHERE dp.childId = :childId
        ORDER BY ml.scheduled_time DESC
    """)
    fun getMealLogsForChild(childId: String): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietaryPlan(plan: DietaryPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealSchedule(schedule: MealSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(log: MealLog)

    @Update
    suspend fun updateMealLog(log: MealLog)
}