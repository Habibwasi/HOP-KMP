package com.example.hop

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
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