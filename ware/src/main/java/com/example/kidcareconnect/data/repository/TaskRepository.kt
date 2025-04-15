package com.example.kidcareconnect.data.repository

import com.example.kidcareconnect.data.local.dao.TaskDao
import com.example.kidcareconnect.data.model.PendingTaskUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) 
{
    // Get all pending tasks
    fun getPendingTasks(): Flow<List<PendingTaskUi>> {
        return taskDao.getPendingTasks().map { tasks ->
            tasks.map { task ->
                PendingTaskUi(
                    id = task.id,
                    childId = task.childId,
                    childName = task.childName,
                    title = task.title,
                    time = task.formattedTime,
                    type = task.type,
                    priority = task.priority
                )
            }
        }
    }

    // Get pending tasks for a specific child
    fun getPendingTasksForChild(childId: String): Flow<List<PendingTaskUi>> {
        return taskDao.getPendingTasksForChild(childId).map { tasks ->
            tasks.map { task ->
                PendingTaskUi(
                    id = task.id,
                    childId = task.childId,
                    childName = task.childName,
                    title = task.title,
                    time = task.formattedTime,
                    type = task.type,
                    priority = task.priority
                )
            }
        }
    }

    // Log a completed task
    suspend fun completeTask(taskId: String) {
        taskDao.updateTaskStatus(taskId, "completed")
    }

    // Log a missed task
    suspend fun missTask(taskId: String) {
        taskDao.updateTaskStatus(taskId, "missed")
    }

    // Reschedule a task
    suspend fun rescheduleTask(taskId: String, newTime: Long) {
        taskDao.updateTaskTime(taskId, newTime)
    }

    // For demo/preview purposes
    companion object {
        fun getPreviewData(): List<PendingTaskUi> {
            return listOf(
                PendingTaskUi(
                    id = "1",
                    childId = "1",
                    childName = "Sarah Johnson",
                    title = "Give Tylenol",
                    time = "10:30 AM",
                    type = "medication",
                    priority = 2
                ),
                PendingTaskUi(
                    id = "2",
                    childId = "3",
                    childName = "Emma Davis",
                    title = "Lunch time",
                    time = "12:00 PM",
                    type = "meal",
                    priority = 1
                ),
                PendingTaskUi(
                    id = "3",
                    childId = "1",
                    childName = "Sarah Johnson",
                    title = "Temperature check",
                    time = "2:00 PM",
                    type = "health",
                    priority = 0
                )
            )
        }
    }
}