package com.example.hop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.data.repository.dev.DevAuthRepository
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.SessionExpiryNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
)

sealed interface AuthEvent {
    data class Register(val phone: String, val firstName: String, val lastName: String, val email: String? = null, val password: String? = null) : AuthEvent
    data class Login(val email: String, val password: String) : AuthEvent
    data object Logout : AuthEvent
    data object ClearError : AuthEvent
    /** Fired on cold start to silently restore a persisted session. */
    data object RestoreSession : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data object NavigateToLogin : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionExpiryNotifier: SessionExpiryNotifier,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>(extraBufferCapacity = 8)
    val effect: Flow<AuthEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionExpiryNotifier.events.collect {
                // Do NOT force-navigate to login. Surface a non-destructive prompt so
                // the user can choose when to re-authenticate.
                _effect.tryEmit(AuthEffect.ShowSnackbar("Your session has expired. Please log in again."))
            }
        }
        // In dev mode, auto-populate the authenticated user so every
        // ViewModel instance (including HomeRoute's) sees currentUser.
        if (authRepository is DevAuthRepository) {
            _state.value = _state.value.copy(
                isAuthenticated = true,
                currentUser = DevAuthRepository.DEV_USER,
            )
        } else {
            // Attempt silent session restore on every cold start.
            // If a refresh token is persisted the user skips the login screen.
            viewModelScope.launch { restoreSession() }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Register -> register(event.phone, event.firstName, event.lastName, event.email, event.password)
            is AuthEvent.Login -> login(event.email, event.password)
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> _state.value = _state.value.copy(error = null)
            is AuthEvent.RestoreSession -> viewModelScope.launch { restoreSession() }
        }
    }

    private fun register(phone: String, firstName: String, lastName: String, email: String?, password: String?) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.register(phone, firstName, lastName, email, password)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = response.data,
                    )
                    _effect.tryEmit(AuthEffect.NavigateToHome)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun login(email: String, password: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.login(email, password)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = response.data,
                    )
                    _effect.tryEmit(AuthEffect.NavigateToHome)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun logout() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.logout()) {
                is ApiResponse.Success -> {
                    _state.value = AuthUiState()
                    _effect.tryEmit(AuthEffect.NavigateToLogin)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                }
            }
        }
    }

    /**
     * Silently restores a persisted session on cold start.
     * Navigates to Home on success; does nothing on failure so the normal
     * Onboarding → Login flow remains visible.
     */
    private suspend fun restoreSession() {
        _state.value = _state.value.copy(isLoading = true)
        when (val response = authRepository.restoreSession()) {
            is ApiResponse.Success -> {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUser = response.data,
                )
                _effect.tryEmit(AuthEffect.NavigateToHome)
            }
            is ApiResponse.Error -> {
                // No stored session or refresh failed — stay on onboarding/login.
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
