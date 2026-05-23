package com.example.hop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.hop.presentation.auth.AuthEffect
import com.example.hop.presentation.auth.AuthViewModel
import kotlinx.coroutines.CompletableDeferred
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

        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> {
                    splashDone.await()
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.NavigateToLogin -> {
                    splashDone.await()
                    navController.navigate(HopRoutes.Onboarding) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.ShowSnackbar -> {
                    // Handled by the root SnackbarHost; no navigation needed.
                }

                is AuthEffect.SessionExpired -> {
                    splashDone.await()
                    navController.navigate(HopRoutes.Onboarding) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.PasswordResetEmailSent -> {
                    // Handled locally in ForgotPasswordRoute; no navigation needed here.
                }

                is AuthEffect.NavigateToSetPassword -> {
                    navController.navigate(HopRoutes.SetNewPassword) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.PasswordUpdated -> {
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.NavigateToDriverSettlement -> {
                    navController.navigate(HopRoutes.DriverSettlementByBooking(bookingId = effect.bookingId))
                }

                is AuthEffect.NavigateToPassengerSettlement -> {
                    navController.navigate(HopRoutes.PassengerSettlement(bookingId = effect.bookingId))
                }
            }
        }
    }
}
