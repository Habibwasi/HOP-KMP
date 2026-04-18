package com.example.hop.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bus that signals a session expiry event from the network layer.
 *
 * The Ktor [HttpSend] interceptor in [HttpClientFactory] calls [notifyExpired] whenever
 * a 401 cannot be recovered via token refresh. [AuthViewModel] observes [events] and
 * forwards the signal as [AuthEffect.SessionExpired].
 *
 * Registered as a Koin singleton so the same instance is shared between the network
 * module and the presentation module.
 */
class SessionExpiryNotifier {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    /** Non-suspending — safe to call from the [HttpSend] intercept block. */
    fun notifyExpired() {
        _events.tryEmit(Unit)
    }
}
