package com.example.hop.chat

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
import kotlinx.serialization.json.Json
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionWebSocketCloseCodeNormalClosure
import platform.Foundation.NSURLSessionWebSocketMessage
import platform.Foundation.NSURL
import platform.Foundation.setValue

/**
 * iOS implementation of [ChatRepository] using [NSURLSessionWebSocketTask].
 *
 * ⚠ This connects to a plain WebSocket endpoint (`wss://`), NOT the Socket.IO
 * protocol. The backend must expose a separate WebSocket endpoint for iOS
 * alongside the Socket.IO endpoint used by Android.
 *
 * Expected endpoint: `wss://api.hop.dk/v1/chat?bookingId=<id>`
 * Auth is passed via the `Authorization` request header (Bearer token).
 */
internal class IosChatRepositoryImpl : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    private val _messages = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 64)
    override val messages: Flow<Message> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var webSocketTask: platform.Foundation.NSURLSessionWebSocketTask? = null

    override fun connect(bookingId: String, token: String) {
        if (_connectionState.value is ConnectionState.Connected ||
            _connectionState.value is ConnectionState.Connecting
        ) return

        _connectionState.value = ConnectionState.Connecting

        val urlString = "wss://api.hop.dk/v1/chat?bookingId=$bookingId"
        val url = NSURL.URLWithString(urlString) ?: run {
            _connectionState.value = ConnectionState.Error("Invalid WebSocket URL")
            return
        }

        val request = NSMutableURLRequest.requestWithURL(url)
        request.setValue("Bearer $token", forHTTPHeaderField = "Authorization")

        webSocketTask = NSURLSession.sharedSession.webSocketTaskWithRequest(request)
        webSocketTask!!.resume()
        // Stay in Connecting — flip to Connected on first successful receive (handshake confirmed)
        receiveNextMessage()
    }

    private fun receiveNextMessage() {
        val task = webSocketTask ?: return
        task.receiveMessageWithCompletionHandler { message, error ->
            if (error != null) {
                _connectionState.value = ConnectionState.Error(
                    error.localizedDescription ?: "WebSocket error"
                )
                return@receiveMessageWithCompletionHandler
            }

            val text = message?.string ?: run {
                // Binary frames unsupported — skip and continue listening
                receiveNextMessage()
                return@receiveMessageWithCompletionHandler
            }

            // Flip to Connected on first successful receive (confirms handshake)
            if (_connectionState.value is ConnectionState.Connecting) {
                _connectionState.value = ConnectionState.Connected
            }

            runCatching { json.decodeFromString<Message>(text) }
                .onSuccess { msg -> scope.launch { _messages.emit(msg) } }

            // Recurse to schedule the next receive
            receiveNextMessage()
        }
    }

    override fun disconnect() {
        webSocketTask?.cancelWithCloseCode(
            closeCode = NSURLSessionWebSocketCloseCodeNormalClosure,
            reason = null,
        )
        webSocketTask = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sendMessage(body: String) {
        if (_connectionState.value !is ConnectionState.Connected) return
        val payload = """{"body":${json.encodeToString(body)}}"""
        val msg = NSURLSessionWebSocketMessage(string = payload)
        webSocketTask?.sendMessage(msg) { _ -> /* fire-and-forget */ }
    }
}
