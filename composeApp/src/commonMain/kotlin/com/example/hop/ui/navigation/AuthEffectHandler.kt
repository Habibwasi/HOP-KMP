package com.example.hop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.hop.presentation.auth.AuthEffect
import com.example.hop.presentation.auth.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

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
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> {
                    navController.navigate(HopRoutes.Home) {
                        popUpTo(HopRoutes.Splash) { inclusive = true }
                    }
                }

                is AuthEffect.NavigateToOtpVerification -> {
                    navController.navigate(HopRoutes.OtpVerification(phone = effect.phone))
                }

                is AuthEffect.NavigateToLogin -> {
                    navController.navigate(HopRoutes.Login) {
                        popUpTo(HopRoutes.Splash) { inclusive = true }
                    }
                }

                is AuthEffect.SessionExpired -> {
                    navController.navigate(HopRoutes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is AuthEffect.ShowSnackbar -> {
                    // Handled by the root SnackbarHost; no navigation needed.
                }
            }
        }
    }
}
