package com.example.kidcareconnect.ui.screens.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidcareconnect.data.AuthManager
import com.example.kidcareconnect.data.repository.ChildRepository
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
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val childRepository: ChildRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotificationEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Get the current user ID from AuthManager
                val userId = authManager.getCurrentUserId() ?: "user1" // Fallback to mock user if not available

                // Load notifications from repository
                notificationRepository.getNotificationsForUser(userId).collect { dbNotifications ->
                    val notificationList = dbNotifications.map { notification ->
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

                    // Count unread notifications
                    val unreadCount = notificationList.count { !it.isRead }

                    _uiState.update { state ->
                        state.copy(
                            notifications = notificationList,
                            unreadCount = unreadCount,
                            isLoading = false
                        )
                    }
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
            try {
                notificationRepository.markNotificationAsRead(notificationId)

                // Update the UI state to reflect the change
                _uiState.update { state ->
                    val updatedNotifications = state.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }

                    state.copy(
                        notifications = updatedNotifications,
                        unreadCount = updatedNotifications.count { !it.isRead }
                    )
                }
            } catch (e: Exception) {
                _events.send(NotificationEvent.ShowMessage("Failed to mark notification as read: ${e.message}"))
            }
        }
    }

    fun onNotificationClicked(notification: NotificationUi) {
        viewModelScope.launch {
            // Mark as read
            if (!notification.isRead) {
                markAsRead(notification.id)
            }

            // Handle navigation based on notification type and associated entities
            when (notification.type) {
                NotificationType.MEDICATION -> {
                    if (notification.childId != null) {
                        _events.send(NotificationEvent.NavigateToChild(notification.childId))
                    } else {
                        _events.send(NotificationEvent.ShowMessage("Medication notification acknowledged"))
                    }
                }
                NotificationType.MEAL -> {
                    if (notification.childId != null) {
                        _events.send(NotificationEvent.NavigateToChild(notification.childId))
                    } else {
                        _events.send(NotificationEvent.ShowMessage("Meal notification acknowledged"))
                    }
                }
                NotificationType.HEALTH -> {
                    if (notification.childId != null) {
                        _events.send(NotificationEvent.NavigateToChild(notification.childId))
                    } else {
                        _events.send(NotificationEvent.ShowMessage("Health notification acknowledged"))
                    }
                }
                NotificationType.SYSTEM -> {
                    _events.send(NotificationEvent.ShowMessage("System notification acknowledged"))
                }
            }
        }
    }

    // Create a new notification (for testing purposes)
    fun createTestNotification(
        title: String,
        message: String,
        type: String,
        priority: Int,
        childId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val userId = authManager.getCurrentUserId() ?: "user1"

                notificationRepository.createNotification(
                    userId = userId,
                    childId = childId,
                    title = title,
                    message = message,
                    type = type,
                    priority = priority,
                    actionId = null
                )

                _events.send(NotificationEvent.ShowMessage("Test notification created"))
                loadNotifications() // Reload to show the new notification
            } catch (e: Exception) {
                _events.send(NotificationEvent.ShowMessage("Failed to create notification: ${e.message}"))
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
}