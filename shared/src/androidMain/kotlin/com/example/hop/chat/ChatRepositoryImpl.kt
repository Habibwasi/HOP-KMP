package com.example.hop.chat

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI

/**
 * Android implementation of [ChatRepository] using socket.io-client.
 *
 * The backend is expected to accept the JWT token via the socket.io `auth` payload
 * and route messages by `bookingId` via a room-join event.
 */
internal class AndroidChatRepositoryImpl : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _messages = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 64)
    override val messages: Flow<Message> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: Socket? = null
    private var currentBookingId: String = ""

    override fun connect(bookingId: String, token: String) {
        // Idempotency guard — avoid duplicate connections
        if (_connectionState.value is ConnectionState.Connected ||
            _connectionState.value is ConnectionState.Connecting
        ) return

        currentBookingId = bookingId
        _connectionState.value = ConnectionState.Connecting

        val opts = IO.Options().apply {
            // Pass JWT as socket.io auth payload (server reads from handshake.auth)
            auth = hashMapOf("token" to token)
        }

        // Connect to the /chat namespace on the API host
        socket = IO.socket(URI.create("https://api.ridly.dk/chat"), opts).also { s ->
            s.on(Socket.EVENT_CONNECT) {
                _connectionState.value = ConnectionState.Connected
                // Join the booking-specific room — gateway expects { bookingId: string }
                s.emit("join", JSONObject().put("bookingId", bookingId))
            }

            s.on(Socket.EVENT_DISCONNECT) {
                _connectionState.value = ConnectionState.Disconnected
            }

            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val reason = args?.getOrNull(0)?.toString() ?: "Connection error"
                _connectionState.value = ConnectionState.Error(reason)
            }

            s.on("message") { args ->
                val json = args?.getOrNull(0) as? JSONObject ?: return@on
                runCatching {
                    Message(
                        id = json.getString("id"),
                        bookingId = json.getString("bookingId"),
                        senderId = json.getString("senderId"),
                        senderName = json.getString("senderName"),
                        body = json.getString("body"),
                        timestampMs = json.getLong("timestampMs"),
                    )
                }.onSuccess { msg ->
                    scope.launch { _messages.emit(msg) }
                }
            }

            s.connect()
        }
    }

    override fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sendMessage(body: String) {
        if (_connectionState.value !is ConnectionState.Connected) return
        val payload = JSONObject().apply {
            put("bookingId", currentBookingId)
            put("body", body)
        }
        socket?.emit("message", payload)
    }
}
