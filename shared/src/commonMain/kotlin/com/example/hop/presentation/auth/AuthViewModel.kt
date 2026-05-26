package com.example.hop.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.data.repository.dev.DevAuthRepository
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.SessionExpiryNotifier
import com.example.hop.network.TokenStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    /** Set to true after a password-reset email is sent; cleared once the recovery deep-link is processed. */
    val isPasswordRecoveryPending: Boolean = false,
    val pendingDeepLinkUrl: String? = null,
    /** Set to true after [AuthEvent.Register] when Supabase requires email confirmation. */
    val isEmailVerificationPending: Boolean = false,
    /** The email address awaiting confirmation; used to display on the verify screen and for resend. */
    val pendingVerificationEmail: String? = null,
    /** Seconds remaining before the resend button re-enables (counts 60 → 0). */
    val resendCooldownSeconds: Int = 0,
)

sealed interface AuthEvent {
    data class Register(val email: String, val password: String, val firstName: String, val lastName: String, val phone: String? = null) : AuthEvent
    data class Login(val email: String, val password: String) : AuthEvent
    data object Logout : AuthEvent
    data object ClearError : AuthEvent
    /** Fired on cold start to silently restore a persisted session. */
    data object RestoreSession : AuthEvent
    /** Fired when the app is opened via the hop://auth/callback email-confirmation deep link. */
    data class HandleDeepLink(val url: String) : AuthEvent
    /**
     * Fired from the platform layer (e.g. MainActivity) to STORE a deep-link URL
     * without processing it. The composable must later dispatch [HandleDeepLink]
     * once the effect collector is live, avoiding the SharedFlow replay-0 race.
     */
    data class QueueDeepLink(val url: String) : AuthEvent
    /** Fired from the Forgot Password screen. */
    data class RequestPasswordReset(val email: String) : AuthEvent
    /** Fired from the Set New Password screen after the user enters a new password. */
    data class UpdatePassword(val newPassword: String) : AuthEvent
    /** Re-fetches the current user profile — use after role changes (e.g. becoming a driver). */
    data object RefreshProfile : AuthEvent
    /** Re-sends the confirmation email; only valid when [AuthUiState.isEmailVerificationPending] is true. */
    data object ResendVerificationEmail : AuthEvent
    /** Fired when the user navigates back from the verify-email screen; clears pending verification state. */
    data object ClearEmailVerification : AuthEvent
    /** Initiates Google OAuth PKCE sign-in. */
    data object SignInWithGoogle : AuthEvent
    /** Initiates Apple OAuth PKCE sign-in. */
    data object SignInWithApple : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data object NavigateToLogin : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
    /** Emitted when a 401 cannot be recovered; all clients should route to Login. */
    data object SessionExpired : AuthEffect
    /** Emitted when a password-reset email has been sent successfully. */
    data object PasswordResetEmailSent : AuthEffect
    /** Emitted when the deep-link callback is a recovery (password-reset) link. Navigate to Set New Password. */
    data object NavigateToSetPassword : AuthEffect
    /** Emitted after the user successfully updates their password. */
    data object PasswordUpdated : AuthEffect
    /** Emitted when a push-notification deep link targets the driver settlement screen. */
    data class NavigateToDriverSettlement(val bookingId: String) : AuthEffect
    /** Emitted when a push-notification deep link targets the passenger settlement screen. */
    data class NavigateToPassengerSettlement(val bookingId: String) : AuthEffect
    /** Emitted after register() when Supabase requires email confirmation before granting a session. */
    data class NavigateToVerifyEmail(val email: String) : AuthEffect
    /** Emitted after the email-confirmation deep link is processed successfully; show the success screen. */
    data object NavigateToEmailVerified : AuthEffect
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionExpiryNotifier: SessionExpiryNotifier,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>(extraBufferCapacity = 8)
    val effect: Flow<AuthEffect> = _effect.asSharedFlow()

    /** Cancellable job that ticks the resend cooldown counter down to zero. */
    private var countdownJob: Job? = null

    init {
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
            // Restore the recovery-pending flag so a cold-start deep-link is handled correctly.
            viewModelScope.launch {
                if (tokenStorage.getRecoveryPending()) {
                    _state.value = _state.value.copy(isPasswordRecoveryPending = true)
                }
            }
        }

        // Observe 401 signals from the network layer and forward as SessionExpired.
        viewModelScope.launch {
            sessionExpiryNotifier.events.collect {
                _state.value = AuthUiState()
                _effect.tryEmit(AuthEffect.SessionExpired)
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Register -> register(event.email, event.password, event.firstName, event.lastName, event.phone)
            is AuthEvent.Login -> login(event.email, event.password)
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> _state.value = _state.value.copy(error = null)
            is AuthEvent.RestoreSession -> viewModelScope.launch { restoreSession() }
            is AuthEvent.QueueDeepLink -> _state.value = _state.value.copy(pendingDeepLinkUrl = event.url)
            is AuthEvent.HandleDeepLink -> {
                _state.value = _state.value.copy(pendingDeepLinkUrl = null)
                handleDeepLink(event.url)
            }
            is AuthEvent.RequestPasswordReset -> requestPasswordReset(event.email)
            is AuthEvent.UpdatePassword -> updatePassword(event.newPassword)
            is AuthEvent.RefreshProfile -> viewModelScope.launch { refreshUser() }
            is AuthEvent.ResendVerificationEmail -> resendVerificationEmail()
            is AuthEvent.ClearEmailVerification -> clearEmailVerification()
            is AuthEvent.SignInWithGoogle -> signInWithGoogle()
            is AuthEvent.SignInWithApple -> signInWithApple()
        }
    }

    private fun handleDeepLink(url: String) {
        // Route push-notification deep links without hitting the auth callback logic.
        if (url.startsWith("ridly://driver-settlement/")) {
            val bookingId = url.removePrefix("ridly://driver-settlement/")
            if (bookingId.isNotBlank()) _effect.tryEmit(AuthEffect.NavigateToDriverSettlement(bookingId))
            return
        }
        if (url.startsWith("ridly://passenger-settlement/")) {
            val bookingId = url.removePrefix("ridly://passenger-settlement/")
            if (bookingId.isNotBlank()) _effect.tryEmit(AuthEffect.NavigateToPassengerSettlement(bookingId))
            return
        }
        viewModelScope.launch {
            val urlIndicatesRecovery = url.contains("type=recovery")
            val storedPending = tokenStorage.getRecoveryPending()
            val statePending = _state.value.isPasswordRecoveryPending
            val isRecovery = statePending || storedPending || urlIndicatesRecovery
            println("[HopDeepLink] handleDeepLink | url=$url | urlIndicatesRecovery=$urlIndicatesRecovery | storedPending=$storedPending | statePending=$statePending | isRecovery=$isRecovery")
            if (isRecovery) {
                if (!statePending) {
                    _state.value = _state.value.copy(isPasswordRecoveryPending = true)
                }
                handleRecoveryCallback(url)
            } else {
                handleAuthCallback(url)
            }
        }
    }

    private fun handleRecoveryCallback(url: String) {
        viewModelScope.launch {
            println("[HopDeepLink] handleRecoveryCallback start | url=$url")
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.handleRecoveryDeepLink(url)) {
                is ApiResponse.Success -> {
                    println("[HopDeepLink] handleRecoveryDeepLink SUCCESS — emitting NavigateToSetPassword")
                    // Clear the flag synchronously before emitting the navigation effect.
                    // A fire-and-forget launch here could be killed before the write
                    // completes if the app crashes immediately after navigation, leaving
                    // a stale flag that mis-routes subsequent deep links.
                    tokenStorage.saveRecoveryPending(false)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isPasswordRecoveryPending = false,
                    )
                    _effect.tryEmit(AuthEffect.NavigateToSetPassword)
                }
                is ApiResponse.Error -> {
                    println("[HopDeepLink] handleRecoveryDeepLink ERROR: ${response.message}")
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun handleAuthCallback(url: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.handleDeepLink(url)) {
                is ApiResponse.Success -> {
                    // Clear any stale recoveryPending flag so a future email-confirmation
                    // deep link is not mistakenly routed to the Set-New-Password screen.
                    tokenStorage.saveRecoveryPending(false)
                    val wasVerificationPending = _state.value.isEmailVerificationPending
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isPasswordRecoveryPending = false,
                        isEmailVerificationPending = false,
                        pendingVerificationEmail = null,
                        resendCooldownSeconds = 0,
                        currentUser = response.data,
                    )
                    countdownJob?.cancel()
                    // If the deep link completed an email verification, show the success
                    // screen; otherwise navigate straight to Home (OAuth or re-login).
                    if (wasVerificationPending) {
                        _effect.tryEmit(AuthEffect.NavigateToEmailVerified)
                    } else {
                        _effect.tryEmit(AuthEffect.NavigateToHome)
                    }
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun register(email: String, password: String, firstName: String, lastName: String, phone: String?) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.register(email, password, firstName, lastName, phone)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = response.data,
                    )
                    _effect.tryEmit(AuthEffect.NavigateToHome)
                }
                is ApiResponse.VerificationRequired -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isEmailVerificationPending = true,
                        pendingVerificationEmail = response.email,
                        resendCooldownSeconds = 60,
                    )
                    _effect.tryEmit(AuthEffect.NavigateToVerifyEmail(response.email))
                    startResendCountdown()
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
            // Optimistically clear auth state immediately so the UI navigates to
            // login without waiting for the Supabase network round-trip.
            tokenStorage.saveRecoveryPending(false)
            _state.value = AuthUiState()
            _effect.tryEmit(AuthEffect.NavigateToLogin)
            // Fire-and-forget: invalidate the Supabase session in the background.
            authRepository.logout()
        }
    }

    private fun requestPasswordReset(email: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.requestPasswordReset(email)) {
                is ApiResponse.Success -> {
                    // Mark recovery as pending so the next deep-link callback is
                    // treated as a recovery link (Supabase PKCE doesn't pass type=recovery in the URL).
                    // Persisted to survive process death so clicking the link after app kill still works.
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isPasswordRecoveryPending = true,
                    )
                    viewModelScope.launch { tokenStorage.saveRecoveryPending(true) }
                    _effect.tryEmit(AuthEffect.PasswordResetEmailSent)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val response = authRepository.updatePassword(newPassword)) {
                is ApiResponse.Success -> {
                    // Belt-and-suspenders: clear the recoveryPending flag here too in
                    // case the user somehow reaches this screen without going through
                    // handleRecoveryCallback (e.g. deep-link race on a fresh install).
                    tokenStorage.saveRecoveryPending(false)
                    _state.value = _state.value.copy(isLoading = false, isPasswordRecoveryPending = false)
                    _effect.tryEmit(AuthEffect.PasswordUpdated)
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
            }
        }
    }

    /**
     * Silently re-fetches the current user after a role change (e.g. becoming a
     * driver). Updates [currentUser] in state without emitting [AuthEffect.NavigateToHome].
     */
    private suspend fun refreshUser() {
        when (val response = authRepository.restoreSession()) {
            is ApiResponse.Success -> {
                _state.value = _state.value.copy(currentUser = response.data)
            }
            is ApiResponse.Error -> Unit // Silently ignore — stay on current screen
        }
    }

    /**
     * Silently restores a persisted session on cold start.
     * Navigates to Home on success; does nothing on failure so the normal
     * Onboarding → Login flow remains visible.
     */
    private suspend fun restoreSession() {
        // Skip if a deep link (e.g. recovery callback) is already queued — processing that
        // deep link will determine the correct destination. NavigateToHome here would race
        // with NavigateToSetPassword and clear the back stack 3 seconds later.
        if (_state.value.pendingDeepLinkUrl != null) return
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

    private fun resendVerificationEmail() {
        val email = _state.value.pendingVerificationEmail ?: return
        viewModelScope.launch {
            when (val response = authRepository.resendVerificationEmail(email)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(resendCooldownSeconds = 60)
                    startResendCountdown()
                    _effect.tryEmit(AuthEffect.ShowSnackbar("Verification email sent!"))
                }
                is ApiResponse.Error -> {
                    _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                }
                else -> Unit
            }
        }
    }

    private fun clearEmailVerification() {
        countdownJob?.cancel()
        _state.value = _state.value.copy(
            isEmailVerificationPending = false,
            pendingVerificationEmail = null,
            resendCooldownSeconds = 0,
        )
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            when (val response = authRepository.signInWithGoogle()) {
                is ApiResponse.Error -> _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                else -> Unit // OAuth browser opened; session arrives via handleDeepLink
            }
        }
    }

    private fun signInWithApple() {
        viewModelScope.launch {
            when (val response = authRepository.signInWithApple()) {
                is ApiResponse.Error -> _effect.tryEmit(AuthEffect.ShowSnackbar(response.message))
                else -> Unit // OAuth browser opened; session arrives via handleDeepLink
            }
        }
    }

    /** Starts (or restarts) the 60-second resend cooldown countdown. */
    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_state.value.resendCooldownSeconds > 0) {
                delay(1_000L)
                _state.value = _state.value.copy(
                    resendCooldownSeconds = (_state.value.resendCooldownSeconds - 1).coerceAtLeast(0),
                )
            }
        }
    }
}
