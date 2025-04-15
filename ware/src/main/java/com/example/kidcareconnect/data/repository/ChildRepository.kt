package com.example.kidcareconnect.data.repository

import com.example.kidcareconnect.data.local.dao.ChildDao
import com.example.kidcareconnect.data.model.ChildUi
import com.google.android.gms.wearable.DataClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepository @Inject constructor(
    private val childDao: ChildDao,
    private val dataClient: DataClient
)
{
    // Get all children assigned to the current caretaker
    fun getAssignedChildren(): Flow<List<ChildUi>> {
        return childDao.getAllAssigned().map { children ->
            children.map { child ->
                ChildUi(
                    id = child.id,
                    name = child.name,
                    age = "${child.age} years",
                    hasPendingTasks = child.hasPendingTasks
                )
            }
        }
    }

    // Get a single child by ID
    fun getChild(childId: String): Flow<ChildUi?> {
        return childDao.getById(childId).map { child ->
            child?.let {
                ChildUi(
                    id = it.id,
                    name = it.name,
                    age = "${it.age} years",
                    hasPendingTasks = it.hasPendingTasks
                )
            }
        }
    }

    // Sync data with the mobile app
    suspend fun syncChildrenData() {
        // Implementation for sync logic with Wearable Data Layer API
        // This would typically query the connected mobile device for updated data
    }

    // For demo/preview purposes when we don't have actual data
    companion object {
        fun getPreviewData(): List<ChildUi> {
            return listOf(
                ChildUi(
                    id = "1",
                    name = "Sarah Johnson",
                    age = "4 years",
                    hasPendingTasks = true
                ),
                ChildUi(
                    id = "2",
                    name = "Michael Lee",
                    age = "3 years",
                    hasPendingTasks = false
                ),
                ChildUi(
                    id = "3",
                    name = "Emma Davis",
                    age = "5 years",
                    hasPendingTasks = true
                ),
                ChildUi(
                    id = "4",
                    name = "Jacob Wilson",
                    age = "2 years",
                    hasPendingTasks = false
                )
            )
        }
    }
}

