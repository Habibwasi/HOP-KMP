package com.example.hop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.hop.domain.repository.UserRepository
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.ui.navigation.AuthEffectHandler
import com.example.hop.ui.navigation.HopNavGraph
import com.example.hop.ui.theme.HopTheme
import com.google.firebase.messaging.FirebaseMessaging
import io.sentry.Sentry
import io.sentry.protocol.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    HopTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = koinViewModel()
        val userRepository: UserRepository = koinInject()
        val state by authViewModel.state.collectAsStateWithLifecycle()
        val appScope = rememberCoroutineScope()

        // Keep Sentry's user scope in sync with the authenticated user so every
        // error report carries a user ID. Cleared on logout so anonymous sessions
        // don't inherit the previous user's identity.
        LaunchedEffect(state.currentUser?.id) {
            val user = state.currentUser
            if (user != null) {
                Sentry.setUser(User().apply { id = user.id })
            } else {
                Sentry.setUser(null)
            }
        }

        // Re-register FCM token after login so the backend has the current token
        // even if onNewToken() fired before the user was authenticated (401 silently swallowed).
        LaunchedEffect(state.isAuthenticated) {
            if (!state.isAuthenticated) return@LaunchedEffect
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                appScope.launch(Dispatchers.IO) {
                    userRepository.savePushToken(token, "android")
                }
            }
        }

        // Process any deep link that arrived before the AuthEffectHandler collector
        // was live. The URL is stored in state by MainActivity (QueueDeepLink) and
        // dispatched here once the composable tree is mounted and effects are active.
        LaunchedEffect(state.pendingDeepLinkUrl) {
            val url = state.pendingDeepLinkUrl ?: return@LaunchedEffect
            authViewModel.onEvent(AuthEvent.HandleDeepLink(url))
        }

        // In dev mode the AuthViewModel auto-populates currentUser, but we
        // still need to fire a login event once so the AuthEffectHandler
        // navigates past the splash screen.
        if (BuildConfig.DEV_MODE) {
            LaunchedEffect(Unit) {
                if (authViewModel.state.value.isAuthenticated) {
                    authViewModel.onEvent(AuthEvent.Login("dev@hop.test", "dev"))
                }
            }
        }

        AuthEffectHandler(
            viewModel = authViewModel,
            navController = navController,
        )

        HopNavGraph(
            navController = navController,
            authViewModel = authViewModel,
            onLogout = {
                // Dispatch the real logout to the activity-scoped ViewModel so
                // authRepository.logout() is called (clears tokens/session).
                // AuthEffectHandler will then receive NavigateToLogin and drive
                // the navigation — no manual Splash trip needed.
                authViewModel.onEvent(AuthEvent.Logout)
            },
        )
    }
}