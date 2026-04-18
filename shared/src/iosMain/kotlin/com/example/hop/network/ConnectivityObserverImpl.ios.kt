package com.example.hop.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue

class ConnectivityObserverImpl : ConnectivityObserver {

    // Optimistic initial state — the NWPathMonitor update handler fires on the
    // dispatch queue immediately after nw_path_monitor_start, correcting this
    // value before the first frame is rendered on the main thread.
    // Seeding true avoids a false-positive amber banner flash on every connected cold start.
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val monitor = nw_path_monitor_create()

    init {
        val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
        nw_path_monitor_set_update_handler(monitor) { path ->
            _isConnected.value = nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }
}
