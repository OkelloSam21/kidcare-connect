//package com.example.kidcareconnect.presentation.service
//
//import android.content.Context
//import androidx.hilt.work.HiltWorker
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.example.kidcareconnect.presentation.data.local.AppDatabase
//import com.example.kidcareconnect.presentation.data.local.entity.Child
//import com.example.kidcareconnect.presentation.data.local.entity.HealthCheck
//import com.example.kidcareconnect.presentation.data.local.entity.Meal
//import com.example.kidcareconnect.presentation.data.local.entity.Medication
//import com.example.kidcareconnect.presentation.data.local.entity.Task
//import com.google.android.gms.wearable.DataClient
//import com.google.android.gms.wearable.DataEvent
//import com.google.android.gms.wearable.DataItem
//import com.google.android.gms.wearable.DataMapItem
//import com.google.android.gms.wearable.Wearable
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//import org.json.JSONArray
//
//@HiltWorker
//class DataSyncWorker @AssistedInject constructor(
//    @Assisted appContext: Context,
//    @Assisted workerParams: WorkerParameters,
//    private val database: AppDatabase
//) : CoroutineWorker(appContext, workerParams)
//{
//
//    private val dataClient: DataClient = Wearable.getDataClient(appContext)
//
//    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//        try {
//            // 1. Pull data from the connected mobile device
//            val dataEvents = dataClient.dataItems.await()
//
//            // 2. Process each data item
//            for (event in dataEvents) {
//                val uri = event.uri
//                val path = uri.path ?: continue
//
//                when {
//                    path.startsWith("/children") -> processChildrenData(event)
//                    path.startsWith("/tasks") -> processTasksData(event)
//                    path.startsWith("/medications") -> processMedicationsData(event)
//                    path.startsWith("/meals") -> processMealsData(event)
//                    path.startsWith("/health-checks") -> processHealthChecksData(event)
//                }
//            }
//
//            // 3. Push local changes back to the mobile device
//            pushLocalChanges()
//
//            Result.success()
//        } catch (e: Exception) {
//            // If work fails, retry with exponential backoff
//            Result.retry()
//        }
//    }
//
//    private suspend fun processChildrenData(event: DataEvent) {
//        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
//        val childrenJson = dataMap.getString("CHILDREN_DATA")
//
//        val jsonArray = JSONArray(childrenJson)
//        val children = mutableListOf<Child>()
//
//        for (i in 0 until jsonArray.length()) {
//            val jsonObject = jsonArray.getJSONObject(i)
//
//            children.add(
//                Child(
//                    id = jsonObject.getString("id"),
//                    name = jsonObject.getString("name"),
//                    age = jsonObject.getInt("age"),
//                    photoUrl = if (jsonObject.has("photoUrl")) jsonObject.getString("photoUrl") else null,
//                    hasPendingTasks = jsonObject.getBoolean("hasPendingTasks"),
//                    isSynced = true,
//                    lastUpdated = System.currentTimeMillis()
//                )
//            )
//        }
//
//        // Save to local database
//        database.childDao().insertAll(children)
//    }
//
//    private suspend fun processTasksData(event: DataEvent) {
//        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
//        val tasksJson = dataMap.getString("TASKS_DATA")
//
//        val jsonArray = JSONArray(tasksJson)
//        val tasks = mutableListOf<Task>()
//
//        for (i in 0 until jsonArray.length()) {
//            val jsonObject = jsonArray.getJSONObject(i)
//
//            tasks.add(
//                Task(
//                    id = jsonObject.getString("id"),
//                    childId = jsonObject.getString("childId"),
//                    childName = jsonObject.getString("childName"),
//                    title = jsonObject.getString("title"),
//                    description = if (jsonObject.has("description")) jsonObject.getString("description") else null,
//                    time = jsonObject.getLong("time"),
//                    type = jsonObject.getString("type"),
//                    status = jsonObject.getString("status"),
//                    priority = jsonObject.getInt("priority")
//                )
//            )
//        }
//
//        // Save to local database
//        database.taskDao().insertAll(tasks)
//    }
//
//    private suspend fun processMedicationsData(event: DataEvent) {
//        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
//        val medicationsJson = dataMap.getString("MEDICATIONS_DATA")
//
//        val jsonArray = JSONArray(medicationsJson)
//        val medications = mutableListOf<Medication>()
//
//        for (i in 0 until jsonArray.length()) {
//            val jsonObject = jsonArray.getJSONObject(i)
//
//            medications.add(
//                Medication(
//                    id = jsonObject.getString("id"),
//                    childId = jsonObject.getString("childId"),
//                    name = jsonObject.getString("name"),
//                    dosage = jsonObject.getString("dosage"),
//                    time = jsonObject.getLong("time"),
//                    frequency = jsonObject.getString("frequency"),
//                    instructions = if (jsonObject.has("instructions")) jsonObject.getString("instructions") else null,
//                    isCritical = jsonObject.getBoolean("isCritical"),
//                    lastAdministered = if (jsonObject.has("lastAdministered")) jsonObject.getLong("lastAdministered") else null
//                )
//            )
//        }
//
//        // Save to local database
//        database.medicationDao().insertAll(medications)
//    }
//
//    private suspend fun processMealsData(event: DataEvent) {
//        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
//        val mealsJson = dataMap.getString("MEALS_DATA")
//
//        val jsonArray = JSONArray(mealsJson)
//        val meals = mutableListOf<Meal>()
//
//        for (i in 0 until jsonArray.length()) {
//            val jsonObject = jsonArray.getJSONObject(i)
//
//            meals.add(
//                Meal(
//                    id = jsonObject.getString("id"),
//                    childId = jsonObject.getString("childId"),
//                    mealType = jsonObject.getString("mealType"),
//                    time = jsonObject.getLong("time"),
//                    dietaryRestrictions = if (jsonObject.has("dietaryRestrictions")) jsonObject.getString("dietaryRestrictions") else null,
//                    allergies = if (jsonObject.has("allergies")) jsonObject.getString("allergies") else null,
//                    lastServed = if (jsonObject.has("lastServed")) jsonObject.getLong("lastServed") else null
//                )
//            )
//        }
//
//        // Save to local database
//        database.mealDao().insertAll(meals)
//    }
//
//    private suspend fun processHealthChecksData(event: DataEvent) {
//        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
//        val healthChecksJson = dataMap.getString("HEALTH_CHECKS_DATA")
//
//        val jsonArray = JSONArray(healthChecksJson)
//        val healthChecks = mutableListOf<HealthCheck>()
//
//        for (i in 0 until jsonArray.length()) {
//            val jsonObject = jsonArray.getJSONObject(i)
//
//            healthChecks.add(
//                HealthCheck(
//                    id = jsonObject.getString("id"),
//                    childId = jsonObject.getString("childId"),
//                    checkType = jsonObject.getString("checkType"),
//                    scheduledTime = jsonObject.getLong("scheduledTime"),
//                    notes = if (jsonObject.has("notes")) jsonObject.getString("notes") else null,
//                    lastChecked = if (jsonObject.has("lastChecked")) jsonObject.getLong("lastChecked") else null
//                )
//            )
//        }
//
//        // Save to local database
//        database.healthCheckDao().insertAll(healthChecks)
//    }
//
//    private suspend fun pushLocalChanges() {
//        // In a real implementation, this would push local changes back to the mobile device
//        // For this example, we'll just outline the basic structure
//
//        // 1. Get local changes that need to be synced
//        // 2. Convert them to data maps
//        // 3. Send them to the connected mobile device
//
//        // Since this is complex and depends on the specific sync strategy,
//        // we'll leave the detailed implementation for a production app
//    }
//}