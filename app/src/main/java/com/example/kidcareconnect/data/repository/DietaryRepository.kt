package com.example.kidcareconnect.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.dao.DietaryPlanDao
import com.example.kidcareconnect.data.local.entities.DietaryPlan
import com.example.kidcareconnect.data.local.entities.MealLog
import com.example.kidcareconnect.data.local.entities.MealSchedule
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.TaskStatus
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietaryRepository @Inject constructor(
    private val dietaryPlanDao: DietaryPlanDao
) {
    fun getDietaryPlanForChild(childId: String) = dietaryPlanDao.getDietaryPlanForChild(childId)
    
    fun getMealSchedulesForPlan(planId: String) = dietaryPlanDao.getMealSchedulesForPlan(planId)
    
    fun getMealLogsForChild(childId: String) = dietaryPlanDao.getMealLogsForChild(childId)
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createDietaryPlan(
        childId: String,
        allergies: String?,
        restrictions: String?,
        preferences: String?,
        notes: String?,
        createdBy: String
    ): DietaryPlan {
        val plan = DietaryPlan(
            planId = UUID.randomUUID().toString(),
            childId = childId,
            allergies = allergies,
            restrictions = restrictions,
            preferences = preferences,
            notes = notes,
            createdBy = createdBy
        )
        dietaryPlanDao.insertDietaryPlan(plan)
        return plan
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createMealSchedule(
        planId: String,
        mealType: MealType,
        time: String,
        days: String,
        menu: String?
    ): MealSchedule {
        val schedule = MealSchedule(
            scheduleId = UUID.randomUUID().toString(),
            planId = planId,
            mealType = mealType,
            time = time,
            days = days,
            menu = menu
        )
        dietaryPlanDao.insertMealSchedule(schedule)
        return schedule
    }
    
    suspend fun logMealService(
        scheduleId: String,
        scheduledTime: LocalDateTime,
        servedTime: LocalDateTime?,
        servedBy: String?,
        status: TaskStatus,
        notes: String?
    ): MealLog {
        val log = MealLog(
            logId = UUID.randomUUID().toString(),
            scheduleId = scheduleId,
            scheduledTime = scheduledTime,
            servedTime = servedTime,
            servedBy = servedBy,
            status = status,
            notes = notes
        )
        dietaryPlanDao.insertMealLog(log)
        return log
    }
    
    suspend fun updateMealLog(log: MealLog) = dietaryPlanDao.updateMealLog(log)
}
