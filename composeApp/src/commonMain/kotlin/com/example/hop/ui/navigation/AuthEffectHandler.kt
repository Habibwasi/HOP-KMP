package com.example.hop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.hop.presentation.auth.AuthEffect
import com.example.hop.presentation.auth.AuthViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Collects [AuthViewModel.effect] and translates each [AuthEffect] into a
 * [NavController] call.
 *
 * Place this at the root composable level alongside [HopNavGraph]:
 *
 * ```kotlin
 * AuthEffectHandler(viewModel = authViewModel, navController = navController)
 * HopNavGraph(navController = navController)
 * ```
 *
 * The snackbar effect is left for a higher-level SnackbarHost; this handler
 * is exclusively concerned with navigation transitions.
 */
@Composable
fun AuthEffectHandler(
    viewModel: AuthViewModel,
    navController: NavController,
) {
    LaunchedEffect(viewModel) {
        // Ensure the splash screen is visible for at least 3 seconds before
        // any auth-triggered navigation fires (handles both dev auto-login and
        // future token-refresh flows).
        val splashDone = CompletableDeferred<Unit>()
        launch { kotlinx.coroutines.delay(3_000); splashDone.complete(Unit) }

        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> {
                    splashDone.await()
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.NavigateToLogin -> {
                    splashDone.await()
                    navController.navigate(HopRoutes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.SessionExpired -> {
                    // Session expiry no longer force-navigates to login.
                    // The user stays on their current screen; individual API calls
                    // will surface errors in-place. They log out explicitly via Settings.
                }

                is AuthEffect.ShowSnackbar -> {
                    // Handled by the root SnackbarHost; no navigation needed.
                }
            }
        }
    }
}
