package com.example.kidcareconnect.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kidcareconnect.data.local.AppDatabase
import com.example.kidcareconnect.data.local.entities.MealSchedule
import com.example.kidcareconnect.data.model.MealType
import com.example.kidcareconnect.data.model.MedicationPriority
import com.example.kidcareconnect.data.model.TaskStatus
import com.example.kidcareconnect.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides mock data for testing and development purposes
 */
@Singleton
class MockDataProvider @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val medicationRepository: MedicationRepository,
    private val dietaryRepository: DietaryRepository,
    private val healthRepository: HealthRepository,
    private val notificationRepository: NotificationRepository,
    private val database: AppDatabase
) {
    private val TAG = "MockDataProvider"

    /**
     * Initialize the database with mock data
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun initializeMockData() = withContext(Dispatchers.IO) {
        try {
            // Check if we already have data
            val users = userRepository.getAllUsers().firstOrNull()
            if (!users.isNullOrEmpty()) {
                // Database already has data, no need to initialize
                Log.d(TAG, "Database already initialized with ${users.size} users")
                return@withContext
            }

            // Create admin user
            val adminUser = userRepository.createUser(
                name = "Dr. Johnson",
                email = "admin@example.com",
                phone = "123-456-7890",
                role = UserRole.ADMIN,
                profilePictureUrl = null
            )

            // Create caretaker users
            val caretaker1 = userRepository.createUser(
                name = "Nurse Williams",
                email = "caretaker@example.com",
                phone = "987-654-3210",
                role = UserRole.CARETAKER,
                profilePictureUrl = null
            )

            val caretaker2 = userRepository.createUser(
                name = "Nurse Garcia",
                email = "nurse2@example.com",
                phone = "555-123-4567",
                role = UserRole.CARETAKER,
                profilePictureUrl = null
            )

            // Create children
            val child1 = childRepository.createChild(
                name = "Sarah Smith",
                dateOfBirth = LocalDate.now().minusYears(5).minusMonths(3),
                gender = "Female",
                bloodGroup = "A+",
                profilePictureUrl = null,
                emergencyContact = "Jane Smith (Mother): 555-987-6543",
                notes = "Likes to draw. Allergic to peanuts."
            )

            val child2 = childRepository.createChild(
                name = "Michael Johnson",
                dateOfBirth = LocalDate.now().minusYears(4).minusMonths(7),
                gender = "Male",
                bloodGroup = "O-",
                profilePictureUrl = null,
                emergencyContact = "Robert Johnson (Father): 555-123-7890",
                notes = "Loves dinosaurs."
            )

            val child3 = childRepository.createChild(
                name = "Emma Davis",
                dateOfBirth = LocalDate.now().minusYears(3).minusMonths(9),
                gender = "Female",
                bloodGroup = "B+",
                profilePictureUrl = null,
                emergencyContact = "Mary Davis (Mother): 555-456-7890",
                notes = "Shy with new people."
            )

            // Assign caretakers to children
            childRepository.assignCaretakerToChild(caretaker1.userId, child1.childId)
            childRepository.assignCaretakerToChild(caretaker1.userId, child2.childId)
            childRepository.assignCaretakerToChild(caretaker2.userId, child3.childId)
            childRepository.assignCaretakerToChild(caretaker2.userId, child1.childId)

            // Create medications for children
            val medication1 = medicationRepository.createMedication(
                childId = child1.childId,
                name = "Allergy Medicine",
                dosage = "5ml",
                instructions = "Give with food",
                startDate = LocalDate.now().minusDays(10),
                endDate = LocalDate.now().plusMonths(1),
                frequency = "daily",
                priority = MedicationPriority.MEDIUM,
                createdBy = adminUser.userId
            )

            val medication2 = medicationRepository.createMedication(
                childId = child2.childId,
                name = "Vitamin D",
                dosage = "1 tablet",
                instructions = "Give after breakfast",
                startDate = LocalDate.now().minusDays(30),
                endDate = null, // ongoing
                frequency = "daily",
                priority = MedicationPriority.LOW,
                createdBy = adminUser.userId
            )

            val medication3 = medicationRepository.createMedication(
                childId = child3.childId,
                name = "Antibiotic",
                dosage = "10ml",
                instructions = "Give 30 minutes before meals",
                startDate = LocalDate.now().minusDays(2),
                endDate = LocalDate.now().plusDays(5),
                frequency = "daily",
                priority = MedicationPriority.HIGH,
                createdBy = adminUser.userId
            )

            // Create medication schedules
            val medicationSchedule1 = medicationRepository.createMedicationSchedule(
                medicationId = medication1.medicationId,
                time = "09:00",
                days = "1,2,3,4,5,6,7" // every day
            )

            val medicationSchedule2 = medicationRepository.createMedicationSchedule(
                medicationId = medication2.medicationId,
                time = "08:30",
                days = "1,2,3,4,5" // weekdays
            )

            val medicationSchedule3a = medicationRepository.createMedicationSchedule(
                medicationId = medication3.medicationId,
                time = "08:00",
                days = "1,2,3,4,5,6,7" // every day
            )

            val medicationSchedule3b = medicationRepository.createMedicationSchedule(
                medicationId = medication3.medicationId,
                time = "16:00",
                days = "1,2,3,4,5,6,7" // every day
            )

            // Create medication logs
            val now = LocalDateTime.now()

            medicationRepository.logMedicationAdministration(
                medicationId = medication1.medicationId,
                scheduledTime = now.minusDays(1).withHour(9).withMinute(0),
                administeredTime = now.minusDays(1).withHour(9).withMinute(5),
                administeredBy = caretaker1.userId,
                status = TaskStatus.COMPLETED,
                notes = "Took medication without issues"
            )

            medicationRepository.logMedicationAdministration(
                medicationId = medication2.medicationId,
                scheduledTime = now.minusDays(1).withHour(8).withMinute(30),
                administeredTime = now.minusDays(1).withHour(8).withMinute(45),
                administeredBy = caretaker2.userId,
                status = TaskStatus.COMPLETED,
                notes = null
            )

            medicationRepository.logMedicationAdministration(
                medicationId = medication3.medicationId,
                scheduledTime = now.minusDays(1).withHour(8).withMinute(0),
                administeredTime = null,
                administeredBy = "null",
                status = TaskStatus.MISSED,
                notes = "Child was absent"
            )

            // Create today's medication logs with PENDING status
            medicationRepository.logMedicationAdministration(
                medicationId = medication1.medicationId,
                scheduledTime = now.withHour(9).withMinute(0),
                administeredTime = null,
                administeredBy = "null",
                status = TaskStatus.PENDING,
                notes = null
            )

            medicationRepository.logMedicationAdministration(
                medicationId = medication2.medicationId,
                scheduledTime = now.withHour(8).withMinute(30),
                administeredTime = null,
                administeredBy = "null",
                status = TaskStatus.PENDING,
                notes = null
            )

            medicationRepository.logMedicationAdministration(
                medicationId = medication3.medicationId,
                scheduledTime = now.withHour(8).withMinute(0),
                administeredTime = null,
                administeredBy = "null",
                status = TaskStatus.PENDING,
                notes = null
            )

            medicationRepository.logMedicationAdministration(
                medicationId = medication3.medicationId,
                scheduledTime = now.withHour(16).withMinute(0),
                administeredTime = null,
                administeredBy = "null",
                status = TaskStatus.PENDING,
                notes = null
            )

            // Create dietary plans
            val dietaryPlan1 = dietaryRepository.createDietaryPlan(
                childId = child1.childId,
                allergies = "Peanuts, Tree nuts",
                restrictions = "No dairy",
                preferences = "Likes fruits, dislikes broccoli",
                notes = "Needs to drink more water throughout the day",
                createdBy = adminUser.userId
            )

            val dietaryPlan2 = dietaryRepository.createDietaryPlan(
                childId = child2.childId,
                allergies = null,
                restrictions = "Vegetarian",
                preferences = "Loves pasta and rice",
                notes = "Picky eater, may need encouragement",
                createdBy = adminUser.userId
            )

            val dietaryPlan3 = dietaryRepository.createDietaryPlan(
                childId = child3.childId,
                allergies = "Shellfish",
                restrictions = null,
                preferences = "Loves vegetables, dislikes spicy food",
                notes = null,
                createdBy = adminUser.userId
            )

            // Create meal schedules
            val mealSchedule1 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan1.planId,
                mealType = MealType.BREAKFAST,
                time = "08:00",
                days = "1,2,3,4,5,6,7",
                menu = "Oatmeal or cereal with fruit"
            )

            val mealSchedule2 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan1.planId,
                mealType = MealType.LUNCH,
                time = "12:00",
                days = "1,2,3,4,5,6,7",
                menu = "Sandwich or rice with vegetables"
            )

            val mealSchedule3 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan1.planId,
                mealType = MealType.SNACK,
                time = "15:00",
                days = "1,2,3,4,5,6,7",
                menu = "Fruit or crackers"
            )

            val mealSchedule4 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan2.planId,
                mealType = MealType.BREAKFAST,
                time = "08:15",
                days = "1,2,3,4,5",
                menu = "Plant-based yogurt with granola"
            )

            val mealSchedule5 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan2.planId,
                mealType = MealType.LUNCH,
                time = "12:15",
                days = "1,2,3,4,5",
                menu = "Vegetarian pasta or rice bowl"
            )

            val mealSchedule6 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan3.planId,
                mealType = MealType.BREAKFAST,
                time = "08:30",
                days = "1,2,3,4,5,6,7",
                menu = "Scrambled eggs with toast"
            )

            val mealSchedule7 = dietaryRepository.createMealSchedule(
                planId = dietaryPlan3.planId,
                mealType = MealType.LUNCH,
                time = "12:30",
                days = "1,2,3,4,5,6,7",
                menu = "Chicken or fish with vegetables"
            )

            // Create meal logs with real schedule IDs
            dietaryRepository.logMealService(
                scheduleId = mealSchedule1.scheduleId,
                scheduledTime = now.minusDays(1).withHour(8).withMinute(0),
                servedTime = now.minusDays(1).withHour(8).withMinute(10),
                servedBy = caretaker1.userId,
                status = TaskStatus.COMPLETED,
                notes = "Ate everything"
            )

            dietaryRepository.logMealService(
                scheduleId = mealSchedule2.scheduleId,
                scheduledTime = now.minusDays(1).withHour(12).withMinute(0),
                servedTime = now.minusDays(1).withHour(12).withMinute(5),
                servedBy = caretaker2.userId,
                status = TaskStatus.COMPLETED,
                notes = "Left some vegetables"
            )

            // Create health logs
            healthRepository.createHealthLog(
                childId = child1.childId,
                temperature = 98.6f,
                heartRate = 90,
                symptoms = null,
                notes = "Regular check-up, all normal",
                loggedBy = caretaker1.userId
            )

            healthRepository.createHealthLog(
                childId = child2.childId,
                temperature = 99.1f,
                heartRate = 95,
                symptoms = "Slight runny nose",
                notes = "Monitoring for possible cold",
                loggedBy = caretaker2.userId
            )

            healthRepository.createHealthLog(
                childId = child3.childId,
                temperature = 100.2f,
                heartRate = 100,
                symptoms = "Cough, fatigue",
                notes = "Started on antibiotics",
                loggedBy = adminUser.userId
            )

            // Create notifications
            notificationRepository.createNotification(
                userId = caretaker1.userId,
                childId = child1.childId,
                title = "Medication Due",
                message = "Sarah's allergy medicine is due at 9:00 AM",
                type = "medication",
                priority = 1,
                actionId = medication1.medicationId
            )

            notificationRepository.createNotification(
                userId = caretaker2.userId,
                childId = child3.childId,
                title = "High Temperature Alert",
                message = "Emma's temperature is 100.2°F. Please monitor.",
                type = "health",
                priority = 2,
                actionId = null
            )

            notificationRepository.createNotification(
                userId = adminUser.userId,
                childId = child2.childId,
                title = "New Prescription Added",
                message = "Vitamin D prescription has been added for Michael",
                type = "medication",
                priority = 0,
                actionId = medication2.medicationId
            )

            Log.d(TAG, "Mock data initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing mock data", e)
            throw e
        }
    }
}