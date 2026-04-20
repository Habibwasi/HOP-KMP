package com.example.hop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.ui.navigation.AuthEffectHandler
import com.example.hop.ui.navigation.HopNavGraph
import com.example.hop.ui.navigation.HopRoutes
import com.example.hop.ui.theme.HopTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    HopTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = koinViewModel()

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
            onLogout = {
                navController.navigate(HopRoutes.Splash) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )
    }
}