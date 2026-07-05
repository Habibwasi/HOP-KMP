package com.example.hop.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ─ State ──────────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val pushNotificationsEnabled: Boolean = true,
    val showLogoutDialog: Boolean = false,
)

// ─ Events ─────────────────────────────────────────────────────────────────────

sealed interface SettingsEvent {
    data object TogglePushNotifications : SettingsEvent
    data object LogoutTapped : SettingsEvent
    data object LogoutConfirmed : SettingsEvent
    data object LogoutDismissed : SettingsEvent
    data object EditProfileTapped : SettingsEvent
    data object ChangePasswordTapped : SettingsEvent
    data object HelpCentreTapped : SettingsEvent
    data object ContactUsTapped : SettingsEvent
    data object TermsOfServiceTapped : SettingsEvent
    data object PrivacyPolicyTapped : SettingsEvent
    data object DeleteAccountTapped : SettingsEvent
}

// ─ Effects ────────────────────────────────────────────────────────────────────

sealed interface SettingsEffect {
    data object NavigateToEditProfile : SettingsEffect
    data object NavigateToChangePassword : SettingsEffect
    data object NavigateToHelpCentre : SettingsEffect
    data object NavigateToContactUs : SettingsEffect
    data object NavigateToTermsOfService : SettingsEffect
    data object NavigateToPrivacyPolicy : SettingsEffect
    data object OpenDeleteAccountEmail : SettingsEffect
    data object Logout : SettingsEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.TogglePushNotifications -> _state.value = _state.value.copy(
                pushNotificationsEnabled = !_state.value.pushNotificationsEnabled,
            )
            is SettingsEvent.LogoutTapped -> _state.value = _state.value.copy(showLogoutDialog = true)
            is SettingsEvent.LogoutDismissed -> _state.value = _state.value.copy(showLogoutDialog = false)
            is SettingsEvent.LogoutConfirmed -> {
                _state.value = _state.value.copy(showLogoutDialog = false)
                viewModelScope.launch { _effect.send(SettingsEffect.Logout) }
            }
            is SettingsEvent.EditProfileTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.NavigateToEditProfile)
            }
            is SettingsEvent.ChangePasswordTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.NavigateToChangePassword)
            }
            is SettingsEvent.HelpCentreTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.NavigateToHelpCentre)
            }
            is SettingsEvent.ContactUsTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.NavigateToContactUs)
            }
            is SettingsEvent.TermsOfServiceTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.NavigateToTermsOfService)
            }
            is SettingsEvent.PrivacyPolicyTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.NavigateToPrivacyPolicy)
            }
            is SettingsEvent.DeleteAccountTapped -> viewModelScope.launch {
                _effect.send(SettingsEffect.OpenDeleteAccountEmail)
            }
        }
    }
}
