package com.example.hop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.SessionExpiryNotifier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val otpSent: Boolean = false,
    /** Phone number waiting for OTP confirmation — set during registration, cleared on verify. */
    val pendingOtpPhone: String = "",
)

sealed interface AuthEvent {
    data class Register(val fullName: String, val email: String, val phone: String, val password: String) : AuthEvent
    data class Login(val email: String, val password: String) : AuthEvent
    data class SendOtp(val phone: String) : AuthEvent
    data class VerifyOtp(val phone: String, val code: String) : AuthEvent
    data object Logout : AuthEvent
    data object ClearError : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data class NavigateToOtpVerification(val phone: String) : AuthEffect
    data object NavigateToLogin : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
    data object SessionExpired : AuthEffect
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionExpiryNotifier: SessionExpiryNotifier,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            sessionExpiryNotifier.events.collect {
                _effect.send(AuthEffect.SessionExpired)
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Register -> register(event.fullName, event.email, event.phone, event.password)
            is AuthEvent.Login -> login(event.email, event.password)
            is AuthEvent.SendOtp -> sendOtp(event.phone)
            is AuthEvent.VerifyOtp -> verifyOtp(event.phone, event.code)
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun register(fullName: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, pendingOtpPhone = phone)
            when (val response = authRepository.register(fullName, email, password)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = response.data,
                    )
                    _effect.send(AuthEffect.NavigateToOtpVerification(phone = phone))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.login(email, password)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = response.data,
                    )
                    _effect.send(AuthEffect.NavigateToHome)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun sendOtp(phone: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.sendOtp(phone)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpSent = true,
                    )
                    _effect.send(AuthEffect.NavigateToOtpVerification(phone))
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.verifyOtp(phone, code)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = response.data,
                        otpSent = false,
                    )
                    _effect.send(AuthEffect.NavigateToHome)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message,
                    )
                    _effect.send(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.logout()) {
                is ApiResponse.Success -> {
                    _state.value = AuthUiState()
                    _effect.send(AuthEffect.NavigateToLogin)
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
}
