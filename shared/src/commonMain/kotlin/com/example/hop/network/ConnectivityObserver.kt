package com.example.hop.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic connectivity observer.
 *
 * [isConnected] emits `true` when the device has an active internet connection and `false` when
 * connectivity is lost. Platform implementations are provided via Koin (see [connectivityModule]).
 *
 * Android: backed by [ConnectivityManager] NetworkCallback.
 * iOS: backed by NWPathMonitor.
 */
interface ConnectivityObserver {
    val isConnected: StateFlow<Boolean>
}
