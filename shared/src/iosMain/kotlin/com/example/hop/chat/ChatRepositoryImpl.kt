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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionWebSocketCloseCodeNormalClosure
import platform.Foundation.NSURLSessionWebSocketMessage
import platform.Foundation.NSURL
import platform.Foundation.setValue

/**
 * iOS implementation of [ChatRepository] using [NSURLSessionWebSocketTask]
 * speaking the Socket.IO v4 (Engine.IO v4) protocol over WebSocket transport.
 *
 * Socket.IO packet format (EIO=4, transport=websocket):
 *   Engine.IO packets:
 *     "0{...}"  — EIO open (server sends sid/pingInterval/pingTimeout)
 *     "2"       — EIO ping (server → client)
 *     "3"       — EIO pong (client → server, in reply to ping)
 *   Socket.IO packets (namespace "/chat"):
 *     "40"            — SIO connect (client sends to join /chat namespace)
 *     "40{...}"       — SIO connect with auth payload
 *     "42[...]"       — SIO event (message)
 *
 * The backend accepts auth via the HTTP `Authorization` header sent during
 * the WebSocket upgrade, so no separate auth payload is needed.
 *
 * Expected connection URL:  wss://api.hop.dk/chat/?EIO=4&transport=websocket
 */
internal class IosChatRepositoryImpl : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    private val _messages = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 64)
    override val messages: Flow<Message> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var webSocketTask: platform.Foundation.NSURLSessionWebSocketTask? = null
    private var bookingId: String = ""

    override fun connect(bookingId: String, token: String) {
        if (_connectionState.value is ConnectionState.Connected ||
            _connectionState.value is ConnectionState.Connecting
        ) return

        this.bookingId = bookingId
        _connectionState.value = ConnectionState.Connecting

        // Socket.IO v4 over WebSocket transport
        val urlString = "wss://api.hop.dk/chat/?EIO=4&transport=websocket"
        val url = NSURL.URLWithString(urlString) ?: run {
            _connectionState.value = ConnectionState.Error("Invalid WebSocket URL")
            return
        }

        val request = NSMutableURLRequest.requestWithURL(url)
        // Pass JWT via Authorization header (read by backend's handleConnection)
        request.setValue("Bearer $token", forHTTPHeaderField = "Authorization")

        webSocketTask = NSURLSession.sharedSession.webSocketTaskWithRequest(request)
        webSocketTask!!.resume()
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
                receiveNextMessage()
                return@receiveMessageWithCompletionHandler
            }

            handlePacket(text)
            receiveNextMessage()
        }
    }

    /** Parse and handle a raw Socket.IO/Engine.IO packet. */
    private fun handlePacket(raw: String) {
        when {
            // EIO open — server sends sid/pingInterval; respond with SIO namespace connect
            raw.startsWith("0") -> {
                // Send SIO connect packet for /chat namespace (no extra auth needed —
                // token is already in the HTTP Authorization header from the upgrade)
                sendRaw("40")
            }

            // EIO ping — server keepalive; reply with pong
            raw == "2" -> sendRaw("3")

            // SIO namespace connect ack — connection is fully established
            raw.startsWith("40") -> {
                _connectionState.value = ConnectionState.Connected
                // Join the booking room
                val joinPayload = buildJsonEvent("join", """{"bookingId":"$bookingId"}""")
                sendRaw("42$joinPayload")
            }

            // SIO event packet — parse the event name and data
            raw.startsWith("42") -> {
                runCatching {
                    val arrayText = raw.substring(2) // strip leading "42"
                    val jsonArray = json.parseToJsonElement(arrayText) as? JsonArray ?: return
                    val eventName = jsonArray[0].jsonPrimitive.content
                    if (eventName == "message") {
                        val msgJson = jsonArray[1].jsonObject
                        Message(
                            id = msgJson["id"]!!.jsonPrimitive.content,
                            bookingId = msgJson["bookingId"]!!.jsonPrimitive.content,
                            senderId = msgJson["senderId"]!!.jsonPrimitive.content,
                            senderName = msgJson["senderName"]!!.jsonPrimitive.content,
                            body = msgJson["body"]!!.jsonPrimitive.content,
                            timestampMs = msgJson["timestampMs"]!!.jsonPrimitive.content.toLong(),
                        )
                    } else null
                }.getOrNull()?.let { msg ->
                    scope.launch { _messages.emit(msg) }
                }
            }
        }
    }

    private fun buildJsonEvent(event: String, dataJson: String): String =
        """["$event",$dataJson]"""

    private fun sendRaw(packet: String) {
        val msg = NSURLSessionWebSocketMessage(string = packet)
        webSocketTask?.sendMessage(msg) { _ -> /* fire-and-forget */ }
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
        val payload = buildJsonEvent("message", """{"bookingId":"$bookingId","body":${json.encodeToString(body)}}""")
        sendRaw("42$payload")
    }
}
