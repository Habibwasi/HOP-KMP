package com.example.hop.domain.repository

import com.example.hop.domain.model.User
import com.example.hop.network.ApiResponse

interface AuthRepository {
    suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String? = null): ApiResponse<User>
    suspend fun login(email: String, password: String): ApiResponse<User>
    suspend fun logout(): ApiResponse<Unit>
    /**
     * Checks if a Supabase session is persisted and fetches the current user from the backend.
     *
     * @return [ApiResponse.Success] with the current user if the session was restored,
     *         [ApiResponse.Error] if there is no stored session.
     */
    suspend fun restoreSession(): ApiResponse<User>

    /**
     * Handles an email-confirmation deep-link callback (hop://auth/callback…).
     * Imports the Supabase session from the URL, then fetches and returns the
     * current user so the caller can navigate to the home screen.
     */
    suspend fun handleDeepLink(url: String): ApiResponse<User>

    /**
     * Handles a password-reset recovery deep-link (hop://auth/callback?type=recovery…).
     * Imports the Supabase recovery session WITHOUT fetching /users/me, because the
     * recovery-scoped JWT cannot access protected endpoints until the password is updated.
     */
    suspend fun handleRecoveryDeepLink(url: String): ApiResponse<Unit>

    /**
     * Sends a password-reset email to [email] via Supabase.
     * The deep-link in the email redirects to hop://auth/callback?type=recovery.
     */
    suspend fun requestPasswordReset(email: String): ApiResponse<Unit>

    /**
     * Updates the current authenticated user's password.
     * Must be called after a recovery deep-link has been handled (session is set).
     */
    suspend fun updatePassword(newPassword: String): ApiResponse<Unit>

    /**
     * Re-sends the Supabase email confirmation link to [email].
     * Only valid while email verification is pending (i.e. after [register] returns
     * [ApiResponse.VerificationRequired]).
     */
    suspend fun resendVerificationEmail(email: String): ApiResponse<Unit>

    /** Initiates a Google OAuth PKCE sign-in flow. Returns immediately; session arrives via deep link. */
    suspend fun signInWithGoogle(): ApiResponse<Unit>

    /** Initiates an Apple OAuth PKCE sign-in flow. Returns immediately; session arrives via deep link. */
    suspend fun signInWithApple(): ApiResponse<Unit>
}
