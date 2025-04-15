package com.example.kidcareconnect.data.sync

import android.content.Context
import android.util.Log
import com.example.kidcareconnect.data.local.AppDatabase
import com.example.kidcareconnect.data.local.entity.Child
import com.example.kidcareconnect.data.local.entity.HealthCheck
import com.example.kidcareconnect.data.local.entity.Meal
import com.example.kidcareconnect.data.local.entity.Medication
import com.example.kidcareconnect.data.local.entity.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DataSyncManager"

@Singleton
class DataSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) : DataClient.OnDataChangedListener {

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Initialize and register for data events
    fun initialize() {
        dataClient.addListener(this)
        requestInitialData()
    }

    // Clean up
    fun cleanup() {
        dataClient.removeListener(this)
    }

    // Request initial data from mobile app
    fun requestInitialData() {
        coroutineScope.launch {
            try {
                // For development/testing: Insert sample data if empty
                if (isLocalDatabaseEmpty()) {
                    Log.d(TAG, "Local database is empty, inserting sample data")
                    insertSampleData()
                }

                // In a real app, this would request data from the mobile device
                val request = PutDataMapRequest.create("/request/initial-data").apply {
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                val putRequest = request.asPutDataRequest().setUrgent()
                dataClient.putDataItem(putRequest).await()
                Log.d(TAG, "Initial data request sent to mobile device")
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting initial data", e)
            }
        }
    }

    // Handle data events from the Data Layer API
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                val path = uri.path ?: return@forEach

                coroutineScope.launch {
                    try {
                        when {
                            path.startsWith("/children") -> processChildrenData(event)
                            path.startsWith("/tasks") -> processTasksData(event)
                            path.startsWith("/medications") -> processMedicationsData(event)
                            path.startsWith("/meals") -> processMealsData(event)
                            path.startsWith("/health-checks") -> processHealthChecksData(event)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing data for path: $path", e)
                    }
                }
            }
        }
    }

    private suspend fun processChildrenData(event: DataEvent) {
        // Implementation would parse and store actual data from the mobile app
        // For now, we'll just ensure we have sample data
        if (isLocalDatabaseEmpty()) {
            insertSampleData()
        }
    }

    private suspend fun processTasksData(event: DataEvent) {
        // Similar implementation for tasks
    }

    private suspend fun processMedicationsData(event: DataEvent) {
        // Similar implementation for medications
    }

    private suspend fun processMealsData(event: DataEvent) {
        // Similar implementation for meals
    }

    private suspend fun processHealthChecksData(event: DataEvent) {
        // Similar implementation for health checks
    }

    private suspend fun isLocalDatabaseEmpty(): Boolean {
        val childrenCount = database.childDao().getCount()
        return childrenCount == 0
    }

    private suspend fun insertSampleData() {
        // Insert sample children
        val children = listOf(
            Child(
                id = "child1",
                name = "Sarah Johnson",
                age = 4,
                photoUrl = null,
                hasPendingTasks = true,
                isSynced = true,
                lastUpdated = System.currentTimeMillis()
            ),
            Child(
                id = "child2",
                name = "Michael Lee",
                age = 3,
                photoUrl = null,
                hasPendingTasks = false,
                isSynced = true,
                lastUpdated = System.currentTimeMillis()
            ),
            Child(
                id = "child3",
                name = "Emma Davis",
                age = 5,
                photoUrl = null,
                hasPendingTasks = true,
                isSynced = true,
                lastUpdated = System.currentTimeMillis()
            )
        )
        database.childDao().insertAll(children)

        // Insert sample tasks
        val tasks = listOf(
            Task(
                id = "task1",
                childId = "child1",
                childName = "Sarah Johnson",
                title = "Give Tylenol",
                description = "5ml dose",
                time = System.currentTimeMillis() + 30 * 60 * 1000, // 30 minutes from now
                type = "medication",
                status = "pending",
                priority = 2
            ),
            Task(
                id = "task2",
                childId = "child3",
                childName = "Emma Davis",
                title = "Lunch time",
                description = "Vegetarian meal",
                time = System.currentTimeMillis() + 60 * 60 * 1000, // 1 hour from now
                type = "meal",
                status = "pending",
                priority = 1
            ),
            Task(
                id = "task3",
                childId = "child1",
                childName = "Sarah Johnson",
                title = "Temperature check",
                description = "Follow up on earlier fever",
                time = System.currentTimeMillis() + 2 * 60 * 60 * 1000, // 2 hours from now
                type = "health",
                status = "pending",
                priority = 0
            )
        )
        database.taskDao().insertAll(tasks)

        // Insert sample medications
        val medications = listOf(
            Medication(
                id = "med1",
                childId = "child1",
                name = "Tylenol",
                dosage = "5ml",
                time = System.currentTimeMillis() + 30 * 60 * 1000, // 30 minutes from now
                frequency = "as_needed",
                instructions = "For fever above 100.4°F",
                isCritical = true,
                lastAdministered = System.currentTimeMillis() - 6 * 60 * 60 * 1000 // 6 hours ago
            ),
            Medication(
                id = "med2",
                childId = "child3",
                name = "Vitamin D",
                dosage = "1 drop",
                time = System.currentTimeMillis() + 3 * 60 * 60 * 1000, // 3 hours from now
                frequency = "daily",
                instructions = "Give with breakfast",
                isCritical = false,
                lastAdministered = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours ago
            )
        )
        database.medicationDao().insertAll(medications)

        // Insert sample meals
        val meals = listOf(
            Meal(
                id = "meal1",
                childId = "child1",
                mealType = "lunch",
                time = System.currentTimeMillis() + 60 * 60 * 1000, // 1 hour from now
                dietaryRestrictions = null,
                allergies = "Nuts",
                lastServed = null
            ),
            Meal(
                id = "meal2",
                childId = "child3",
                mealType = "lunch",
                time = System.currentTimeMillis() + 60 * 60 * 1000, // 1 hour from now
                dietaryRestrictions = "Vegetarian",
                allergies = "Dairy",
                lastServed = null
            )
        )
        database.mealDao().insertAll(meals)

        // Insert sample health checks
        val healthChecks = listOf(
            HealthCheck(
                id = "check1",
                childId = "child1",
                checkType = "temperature",
                scheduledTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000, // 2 hours from now
                notes = "Follow up on morning fever",
                lastChecked = System.currentTimeMillis() - 4 * 60 * 60 * 1000 // 4 hours ago
            ),
            HealthCheck(
                id = "check2",
                childId = "child2",
                checkType = "diaper",
                scheduledTime = System.currentTimeMillis() + 30 * 60 * 1000, // 30 minutes from now
                notes = null,
                lastChecked = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // 2 hours ago
            )
        )
        database.healthCheckDao().insertAll(healthChecks)

        Log.d(TAG, "Sample data inserted successfully")
    }
}