package com.example.hop.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.HopNotification
import com.example.hop.domain.model.NotificationType
import com.example.hop.domain.repository.UserRepository
import com.example.hop.network.ApiResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<HopNotification> = emptyList(),
    val error: String? = null,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface NotificationsEvent {
    data object Load : NotificationsEvent
    data class MarkRead(val notificationId: String) : NotificationsEvent
    data object GoToSearchTapped : NotificationsEvent
    data class Tapped(val notification: HopNotification) : NotificationsEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface NotificationsEffect {
    data object NavigateToSearch : NotificationsEffect
    data class NavigateToDriverSettlement(val bookingId: String) : NotificationsEffect
    data class NavigateToPassengerSettlement(val bookingId: String) : NotificationsEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class NotificationsViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _effect = Channel<NotificationsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            is NotificationsEvent.Load -> load()
            is NotificationsEvent.MarkRead -> markRead(event.notificationId)
            is NotificationsEvent.GoToSearchTapped -> viewModelScope.launch {
                _effect.send(NotificationsEffect.NavigateToSearch)
            }
            is NotificationsEvent.Tapped -> onNotificationTapped(event.notification)
        }
    }

    private fun load() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = userRepository.getNotifications()) {
                is ApiResponse.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    notifications = response.data,
                )
                is ApiResponse.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = response.message,
                )
            }
        }
    }

    private fun onNotificationTapped(notification: HopNotification) {
        markRead(notification.id)
        val bookingId = notification.deepLinkId ?: return
        viewModelScope.launch {
            when (notification.type) {
                NotificationType.PAYMENT_MARKED_PAID ->
                    _effect.send(NotificationsEffect.NavigateToDriverSettlement(bookingId))
                NotificationType.PAYMENT_CONFIRMED ->
                    _effect.send(NotificationsEffect.NavigateToPassengerSettlement(bookingId))
                else -> { /* no deep-link navigation for this type */ }
            }
        }
    }

    private fun markRead(notificationId: String) {
        if (_state.value.notifications.find { it.id == notificationId }?.isRead == true) return
        // Optimistically mark read in UI state
        _state.value = _state.value.copy(
            notifications = _state.value.notifications.map { notification ->
                if (notification.id == notificationId) notification.copy(isRead = true)
                else notification
            },
        )
        viewModelScope.launch {
            val result = userRepository.markNotificationRead(notificationId)
            if (result is ApiResponse.Error) {
                // Rollback optimistic update on failure
                _state.value = _state.value.copy(
                    notifications = _state.value.notifications.map { notification ->
                        if (notification.id == notificationId) notification.copy(isRead = false)
                        else notification
                    },
                )
            }
        }
    }
}
