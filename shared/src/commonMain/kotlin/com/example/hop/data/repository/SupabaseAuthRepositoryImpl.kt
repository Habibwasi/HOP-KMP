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
import kotlinx.serialization.json.buildJsonObject
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
    ): ApiResponse<User> = safeApiCall {
        // Store profile data as Supabase user metadata so the backend guard can
        // auto-create the Prisma profile on first login even if email confirmation
        // delays the initial profile POST.
        supabase.auth.signUpWith(Email, redirectUrl = "ridly://auth/callback") {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("firstName", firstName)
                put("lastName", lastName)
                if (phone != null) put("phone", phone)
            }
        }

        // Wait until the Auth plugin has committed the session to its StateFlow.
        // signUpWith is async-internal in supabase-kt v3 — poll sessionStatus until
        // it settles so currentSessionOrNull() is reliable before the first request.
        val status = supabase.auth.sessionStatus.first {
            it !is SessionStatus.Initializing
        }
        val token = (status as? SessionStatus.Authenticated)?.session?.accessToken
        if (token != null) {
            // When email confirmation is ON, Supabase may return a session whose
            // JWT is rejected by the backend ("Email not confirmed"). Detect this
            // early and sign out cleanly before the 401 is ever sent to the UI.
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser?.emailConfirmedAt == null) {
                supabase.auth.signOut()
                throw Exception("Account created! Please check your email to confirm, then log in.")
            }

            // Email confirmation is OFF — we have a verified session immediately.
            // POST /users/profile WITHOUT an explicit Authorization header — the
            // AuthInterceptor (which now uses header set, not append) will add it.
            // If profile creation fails (e.g. phone conflict), the backend deletes
            // the dangling Supabase auth user. Sign out locally to clear the orphaned
            // session so the user can re-register without "user already exists".
            val envelope = runCatching {
                httpClient.post("users/profile") {
                    setBody(CreateProfileRequest(firstName, lastName, phone, email))
                }.body<ApiEnvelope<UserDto>>()
            }.getOrElse { e ->
                supabase.auth.signOut()
                throw e
            }
            val err = envelope.error
            if (err != null) {
                supabase.auth.signOut()
                throw Exception(err.message)
            }
            return@safeApiCall checkNotNull(envelope.data) { "Null data in /users/profile response" }.toDomain()
        }

        // Email confirmation is ON — the guard will auto-create the Prisma profile
        // from user_metadata on the user's first authenticated request after
        // they confirm their email. Show a message to prompt confirmation.
        throw Exception("Account created! Please check your email to confirm, then log in.")
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
        supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
        val envelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
        val error = envelope.error
        if (error != null) throw Exception(error.message)
        checkNotNull(envelope.data) { "Null data in /users/me response" }.toDomain()
    }

    override suspend fun logout(): ApiResponse<Unit> = safeApiCall {
        supabase.auth.signOut()
    }

    override suspend fun restoreSession(): ApiResponse<User> {
        // On cold start in supabase-kt v3 / Android, the session is loaded from
        // DataStore asynchronously. Wait for that load to complete before reading
        // the session — otherwise currentSessionOrNull() races and returns null.
        supabase.auth.sessionStatus.first { it !is SessionStatus.Initializing }
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
        // Supabase email-confirmation callbacks come in two flavours:
        //  • Implicit flow  → ridly://auth/callback#access_token=TOKEN&refresh_token=…
        //  • PKCE / code flow → ridly://auth/callback?code=CODE
        val parsedUrl = Url(url)
        val code = parsedUrl.parameters["code"]
        if (code != null) {
            supabase.auth.exchangeCodeForSession(code)
        } else {
            val fragment = url.substringAfter("#", "")
            check(fragment.contains("access_token")) { "Unrecognised auth callback URL" }
            // supabase-kt v3 removed parseFragmentAndImportSession.
            // Parse the access_token / refresh_token from the fragment and import them directly.
            val fragmentParams = parseQueryString(fragment)
            val accessToken = fragmentParams["access_token"]
                ?: error("No access_token in auth callback fragment")
            val refreshToken = fragmentParams["refresh_token"] ?: ""
            supabase.auth.importAuthToken(accessToken, refreshToken, retrieveUser = false, autoRefresh = true)
        }
        // Wait for the Auth plugin to commit the newly imported session.
        supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
        val envelope = httpClient.get("users/me").body<ApiEnvelope<UserDto>>()
        val error = envelope.error
        if (error != null) throw Exception(error.message)
        checkNotNull(envelope.data) { "Null data in /users/me response" }.toDomain()
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
            supabase.auth.importAuthToken(accessToken, refreshToken, retrieveUser = false, autoRefresh = true)
        }
        // Wait for the session to be committed — recovery sessions are still
        // SessionStatus.Authenticated, but the JWT role is "recovery".
        supabase.auth.sessionStatus.first { it is SessionStatus.Authenticated }
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
}
