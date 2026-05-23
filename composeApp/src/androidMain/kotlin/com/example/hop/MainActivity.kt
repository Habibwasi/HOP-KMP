package com.example.hop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hop.presentation.auth.AuthEvent
import com.example.hop.presentation.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("HopDeepLink", "onCreate | action=${intent?.action} | data=${intent?.data}")
        handleDeepLinkIntent(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("HopDeepLink", "onNewIntent | action=${intent.action} | data=${intent.data}")
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val url = intent?.data?.toString()
        Log.d("HopDeepLink", "handleDeepLinkIntent | url=$url")
        if (url == null) return
        if (url.startsWith("hop://auth/callback")) {
            Log.d("HopDeepLink", "Queueing auth deep link: $url")
            authViewModel.onEvent(AuthEvent.QueueDeepLink(url))
        } else if (url.startsWith("hop://driver-settlement/")) {
            Log.d("HopDeepLink", "Queueing driver-settlement deep link: $url")
            authViewModel.onEvent(AuthEvent.QueueDeepLink(url))
        } else if (url.startsWith("hop://passenger-settlement/")) {
            Log.d("HopDeepLink", "Queueing passenger-settlement deep link: $url")
            authViewModel.onEvent(AuthEvent.QueueDeepLink(url))
        } else {
            Log.w("HopDeepLink", "URL does not match any known scheme — ignored: $url")
        }
    }
}
