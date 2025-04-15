package com.example.kidcareconnect.ui.screens.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// UI model for notifications
data class NotificationUi(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val priority: Int,
    val time: String,
    val isRead: Boolean,
    val childId: String?,
    val actionId: String?
)

enum class NotificationType {
    MEDICATION, MEAL, HEALTH, SYSTEM;

    companion object {
        fun fromString(value: String): NotificationType {
            return when (value.lowercase()) {
                "medication" -> MEDICATION
                "meal" -> MEAL
                "health" -> HEALTH
                else -> SYSTEM
            }
        }

    }
}

// Events from Notifications Screen
sealed class NotificationEvent {
    data class NavigateToChild(val childId: String) : NotificationEvent()
    data class ShowMessage(val message: String) : NotificationEvent()
}

// UI State for Notifications Screen
data class NotificationsUiState(
    val notifications: List<NotificationUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotificationEvent>()
    val events = _events.receiveAsFlow()

    // Mock current user for development
    private val mockUserId = "user1"

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val dbNotifications =
                    notificationRepository.getNotificationsForUser(mockUserId).firstOrNull()
                val notificationList = if (dbNotifications.isNullOrEmpty()) {
                    getDefaultNotifications()

                } else {
                    dbNotifications.map { notification ->
                        NotificationUi(
                            id = notification.notificationId,
                            title = notification.title,
                            message = notification.message,
                            type = NotificationType.fromString(notification.type),
                            priority = notification.priority,
                            time = formatDateTime(notification.createdAt),
                            isRead = notification.isRead,
                            childId = notification.childId,
                            actionId = notification.actionId
                        )
                    }

                }
                _uiState.update { state ->
                    state.copy(
                        notifications = notificationList,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading notifications"
                    )
                }
            }


        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notificationId)
        }
    }

    fun onNotificationClicked(notification: NotificationUi) {
        viewModelScope.launch {
            // Mark as read first
            markAsRead(notification.id)

            // If there's a child associated, navigate to child profile
            if (notification.childId != null) {
                _events.send(NotificationEvent.NavigateToChild(notification.childId))
            } else {
                // Just show a message if there's no specific navigation
                _events.send(NotificationEvent.ShowMessage("Notification acknowledged"))
            }
        }
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

        return when {
            dateTime.toLocalDate() == now.toLocalDate() -> {
                "Today at ${dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
            }

            dateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> {
                "Yesterday at ${dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
            }

            else -> {
                dateTime.format(formatter)
            }
        }
    }

    private fun getDefaultNotifications(): List<NotificationUi> {
        val now = LocalDateTime.now()

        return listOf(
            NotificationUi(
                id = "notification1",
                title = "Medication Reminder",
                message = "Emma's antibiotic is due in 15 minutes",
                type = NotificationType.MEDICATION,
                priority = 2,
                time = formatDateTime(now.minusMinutes(5)),
                isRead = false,
                childId = "child1",
                actionId = "medication1"
            ),
            NotificationUi(
                id = "notification2",
                title = "Meal Time",
                message = "Lunch is scheduled for 12:00 PM",
                type = NotificationType.MEAL,
                priority = 1,
                time = formatDateTime(now.minusHours(2)),
                isRead = true,
                childId = null,
                actionId = "meal1"
            ),
            NotificationUi(
                id = "notification3",
                title = "Health Update",
                message = "Noah's temperature is normal",
                type = NotificationType.HEALTH,
                priority = 0,
                time = formatDateTime(now.minusHours(3)),
                isRead = false,
                childId = "child2",
                actionId = "health1"
            )
        )
    }
}