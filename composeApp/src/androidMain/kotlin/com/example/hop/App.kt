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

        // In dev mode, auto-login with dummy credentials so the splash/login
        // screens are skipped and you land directly on Home.
        if (BuildConfig.DEV_MODE) {
            LaunchedEffect(Unit) {
                authViewModel.onEvent(AuthEvent.Login("dev@hop.test", "dev"))
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