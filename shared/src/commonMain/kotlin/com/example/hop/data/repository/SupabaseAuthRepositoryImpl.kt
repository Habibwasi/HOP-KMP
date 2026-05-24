package com.example.hop.data.repository

import com.example.hop.data.dto.ApiEnvelope
import com.example.hop.data.dto.CreateProfileRequest
import com.example.hop.data.dto.UserDto
import com.example.hop.data.dto.toDomain
import com.example.hop.domain.model.User
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.network.ApiResponse
import com.example.hop.network.safeApiCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Url
import io.ktor.http.parseQueryString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class SupabaseAuthRepositoryImpl(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
    ): ApiResponse<User> {
        // Step 1: call signUpWith — this may or may not produce an immediate session
        // depending on whether email confirmation is enabled in Supabase.
        val signUpResult = safeApiCall {
            supabase.auth.signUpWith(Email, redirectUrl = "ridly://auth/callback") {
                this.email = email
                this.password = password
                // Store profile metadata so the backend guard can auto-create the
                // Prisma profile on the first verified request.
                this.data = buildJsonObject {
                    put("firstName", firstName)
                    put("lastName", lastName)
                    if (phone != null) put("phone", phone)
                }
            }
        }
        if (signUpResult is ApiResponse.Error) return signUpResult

        // Step 2: poll for session settlement (15-second timeout).
        val status = withTimeoutOrNull(15_000L) {
            supabase.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        } ?: return ApiResponse.Error(-1, "Authentication timed out. Please check your connection and try again.")

        val token = (status as? SessionStatus.Authenticated)?.session?.accessToken
        if (token != null) {
            // We got an immediate session — check if email is already confirmed.
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser?.emailConfirmedAt == null) {
                // Email confirmation is ON and we received an unconfirmed session.
                // Sign out to discard the unconfirmed session; the user must click
                // the confirmation link to get a valid session.
                supabase.auth.signOut()
                return ApiResponse.VerificationRequired(email)
            }

            // Email confirmation is OFF — create Prisma profile immediately.
            val envelope = runCatching {
                httpClient.post("users/profile") {
                    setBody(CreateProfileRequest(firstName, lastName, phone, email))
                }.body<ApiEnvelope<UserDto>>()
            }.getOrElse { e ->
                supabase.auth.signOut()
                return ApiResponse.Error(-1, e.message ?: "Profile creation failed")
            }
            val err = envelope.error
            if (err != null) {
                supabase.auth.signOut()
                return ApiResponse.Error(-1, err.message)
            }
            val user = checkNotNull(envelope.data) { "Null data in /users/profile response" }.toDomain()
            return ApiResponse.Success(user)
        }

        // No session produced — email confirmation is required.
        return ApiResponse.VerificationRequired(email)
    }

    override suspend fun login(
        email: String,
        password: String,
    ): ApiResponse<User> = safeApiCall {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        // Wait for the session StateFlow to emit Authenticated before the GET so
        // AuthInterceptor can reliably read currentSessionOrNull().
        // Timeout prevents the coroutine hanging forever if the state machine stalls.
        withTimeoutOrNull(15_000L) {
            supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
        } ?: throw Exception("Sign-in timed out. Please check your connection and try again.")
        fetchOrCreateProfile()
    }

    override suspend fun logout(): ApiResponse<Unit> = safeApiCall {
        supabase.auth.signOut()
    }

    override suspend fun restoreSession(): ApiResponse<User> {
        // On cold start in supabase-kt v3 / Android, the session is loaded from
        // DataStore asynchronously. Wait for that load to complete before reading
        // the session — otherwise currentSessionOrNull() races and returns null.
        withTimeoutOrNull(15_000L) {
            supabase.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        } ?: return ApiResponse.Error(-3, "Session restore timed out. Please log in again.")
        val session = supabase.auth.currentSessionOrNull()
            ?: return ApiResponse.Error(-1, "No stored session")
        return safeApiCall {
            val envelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
            val error = envelope.error
            if (error != null) throw Exception(error.message)
            checkNotNull(envelope.data) { "Null data in /users/me response" }.toDomain()
        }
    }

    override suspend fun handleDeepLink(url: String): ApiResponse<User> = safeApiCall {
        // Supabase email-confirmation callbacks come in three flavours:
        //  • Server-side verify  → ridly://auth/callback?access_token=TOKEN&refresh_token=…
        //  • PKCE / code flow    → ridly://auth/callback?code=CODE
        //  • Implicit flow       → ridly://auth/callback#access_token=TOKEN&refresh_token=…
        val parsedUrl = Url(url)

        // Ktor's Url parser may not populate .parameters for custom URL schemes on
        // Kotlin/Native (iOS).  Parse the raw query string manually as a fallback so
        // the same code works on both Android (JVM) and iOS (K/N).
        val rawQuery = url.substringAfter("?", "").substringBefore("#")
        fun queryParam(name: String): String? =
            parsedUrl.parameters[name]
                ?: rawQuery.split("&")
                    .firstOrNull { it.startsWith("$name=") }
                    ?.substringAfter("=")

        // Surface Supabase error redirects (e.g. otp_expired) as readable exceptions.
        val errorParam = queryParam("error")
        if (errorParam != null) {
            val desc = (queryParam("error_description") ?: errorParam).replace('+', ' ')
            error(desc)
        }
        val code = queryParam("code")
        // access_token passed as a query parameter (server-side verification flow):
        // our /auth/confirm-email endpoint verifies the token_hash server-side and
        // redirects to ridly://auth/callback?access_token=...&refresh_token=...
        val accessTokenParam = queryParam("access_token")
        if (code != null) {
            supabase.auth.exchangeCodeForSession(code)
        } else if (accessTokenParam != null) {
            val refreshToken = queryParam("refresh_token") ?: ""
            supabase.auth.importAuthToken(
                accessTokenParam,
                refreshToken,
                retrieveUser = false,
                autoRefresh = refreshToken.isNotEmpty(),
            )
        } else {
            val fragment = url.substringAfter("#", "")
            check(fragment.contains("access_token")) {
                "Unrecognised auth callback URL — received: $url"
            }
            // supabase-kt v3 removed parseFragmentAndImportSession.
            // Parse the access_token / refresh_token from the fragment and import them directly.
            val fragmentParams = parseQueryString(fragment)
            val accessToken = fragmentParams["access_token"]
                ?: error("No access_token in auth callback fragment")
            val refreshToken = fragmentParams["refresh_token"] ?: ""
            // Implicit-flow confirmation links sometimes omit the refresh_token.
            // Enabling autoRefresh with an empty token causes the library to attempt
            // a silent refresh that will always fail, producing a phantom 401 and an
            // unexpected logout when the access token expires. Only enable autoRefresh
            // when we actually have a refresh token to use.
            supabase.auth.importAuthToken(
                accessToken,
                refreshToken,
                retrieveUser = false,
                autoRefresh = refreshToken.isNotEmpty(),
            )
        }
        // Wait for the Auth plugin to commit the newly imported session.
        withTimeoutOrNull(15_000L) {
            supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
        } ?: throw Exception("Authentication timed out. The link may have expired — please try again.")
        fetchOrCreateProfile()
    }

    override suspend fun handleRecoveryDeepLink(url: String): ApiResponse<Unit> = safeApiCall {
        val parsedUrl = Url(url)
        val code = parsedUrl.parameters["code"]
        if (code != null) {
            supabase.auth.exchangeCodeForSession(code)
        } else {
            val fragment = url.substringAfter("#", "")
            check(fragment.contains("access_token")) { "Unrecognised recovery callback URL" }
            val fragmentParams = parseQueryString(fragment)
            val accessToken = fragmentParams["access_token"]
                ?: error("No access_token in recovery callback fragment")
            val refreshToken = fragmentParams["refresh_token"] ?: ""
            // Same guard as handleDeepLink: only enable autoRefresh when a refresh
            // token is actually present so a missing token does not cause silent
            // failed-refresh loops and a phantom SessionExpired event.
            supabase.auth.importAuthToken(
                accessToken,
                refreshToken,
                retrieveUser = false,
                autoRefresh = refreshToken.isNotEmpty(),
            )
        }
        // Wait for the session to be committed — recovery sessions are still
        // SessionStatus.Authenticated, but the JWT role is "recovery".
        withTimeoutOrNull(15_000L) {
            supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
        } ?: throw Exception("Password reset timed out. The link may have expired — please request a new one.")
    }

    override suspend fun requestPasswordReset(email: String): ApiResponse<Unit> = safeApiCall {
        supabase.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "ridly://auth/callback",
        )
    }

    override suspend fun updatePassword(newPassword: String): ApiResponse<Unit> = safeApiCall {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    override suspend fun resendVerificationEmail(email: String): ApiResponse<Unit> = safeApiCall {
        supabase.auth.resendEmail(OtpType.Email.SIGNUP, email)
    }

    override suspend fun signInWithGoogle(): ApiResponse<Unit> = safeApiCall {
        supabase.auth.signInWith(Google, redirectUrl = "ridly://auth/callback")
    }

    override suspend fun signInWithApple(): ApiResponse<Unit> = safeApiCall {
        supabase.auth.signInWith(Apple, redirectUrl = "ridly://auth/callback")
    }

    /**
     * Fetches the user profile from the backend. If the profile does not exist yet
     * (first login after email confirmation), it is created using the metadata
     * stored in Supabase during registration (firstName, lastName, phone).
     */
    private suspend fun fetchOrCreateProfile(): User {
        val meEnvelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
        if (meEnvelope.error == null && meEnvelope.data != null) {
            return meEnvelope.data.toDomain()
        }
        // Profile not found — first login after email confirmation.
        // Create the Prisma profile using metadata set during sign-up.
        val supabaseUser = supabase.auth.currentUserOrNull()
            ?: throw Exception("No authenticated user found.")
        val meta = supabaseUser.userMetadata
        val firstName = meta?.get("firstName")?.jsonPrimitive?.contentOrNull ?: ""
        val lastName  = meta?.get("lastName")?.jsonPrimitive?.contentOrNull ?: ""
        val phone     = meta?.get("phone")?.jsonPrimitive?.contentOrNull
        val email     = supabaseUser.email ?: throw Exception("User email not found in session.")
        val createEnvelope = httpClient.post("users/profile") {
            setBody(CreateProfileRequest(firstName, lastName, phone, email))
        }.body<ApiEnvelope<UserDto>>()
        val createError = createEnvelope.error
        if (createError != null) throw Exception(createError.message)
        return checkNotNull(createEnvelope.data) { "Null data in /users/profile response" }.toDomain()
    }
}
