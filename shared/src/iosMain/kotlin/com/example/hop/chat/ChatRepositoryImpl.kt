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
 * Socket.IO packet format (EIO=4, transport=websocket, namespace="/chat"):
 *   Engine.IO packets:
 *     "0{...}"          — EIO open (server sends sid/pingInterval/pingTimeout)
 *     "2"               — EIO ping (server → client)
 *     "3"               — EIO pong (client → server, in reply to ping)
 *   Socket.IO packets:
 *     "40/chat,"        — SIO CONNECT to /chat namespace (client → server)
 *     "40/chat,{...}"   — SIO CONNECT ack from server
 *     "42/chat,[...]"   — SIO EVENT on /chat namespace
 *
 * NOTE: the namespace "/chat" MUST be included in every SIO packet.
 * Sending "40" (no namespace) connects to the default "/" namespace which
 * has no handlers — the /chat gateway is unreachable that way.
 *
 * Auth is passed via the HTTP Authorization header on the WebSocket upgrade
 * request (NestJS ChatGateway reads handshake.headers.authorization).
 *
 * Engine.IO path: /socket.io  (NestJS default; /chat is the SIO namespace,
 * not the HTTP path)
 *
 * Expected connection URL:  wss://hop.ridly.dk/socket.io/?EIO=4&transport=websocket
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

        // Socket.IO v4 over WebSocket transport.
        // The Engine.IO path is /socket.io (NestJS default).
        // The /chat segment is the Socket.IO namespace, NOT the HTTP path.
        val urlString = "wss://hop.ridly.dk/socket.io/?EIO=4&transport=websocket"
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
                // Connect to the /chat namespace. Auth token is already in the
                // Authorization header of the HTTP upgrade request.
                sendRaw("40/chat,")
            }

            // EIO ping — server keepalive; reply with pong
            raw == "2" -> sendRaw("3")

            // SIO /chat namespace connect ack — connection is fully established
            raw.startsWith("40/chat,") -> {
                _connectionState.value = ConnectionState.Connected
                // Join the booking room — gateway expects { bookingId: string }
                sendRaw("42/chat,${buildJsonEvent("join", """{"bookingId":"$bookingId"}""")}")
            }

            // SIO event on /chat namespace — e.g. "42/chat,["message",{...}]"
            raw.startsWith("42/chat,") -> {
                runCatching {
                    val arrayText = raw.substring("42/chat,".length) // strip "42/chat,"
                    val jsonArray = json.parseToJsonElement(arrayText) as? JsonArray ?: return
                    val eventName = jsonArray[0].jsonPrimitive.content
                    when (eventName) {
                        "message" -> {
                            val msgJson = jsonArray[1].jsonObject
                            listOf(parseMessage(msgJson))
                        }
                        "history" -> {
                            // Server pushes history as an array under event "history"
                            val arr = jsonArray[1] as? kotlinx.serialization.json.JsonArray
                            arr?.mapNotNull { runCatching { parseMessage(it.jsonObject) }.getOrNull() }
                        }
                        // NestJS WsException — surface as connection error so the banner shows
                        "exception" -> {
                            val msg = runCatching {
                                jsonArray[1].jsonObject["message"]?.jsonPrimitive?.content
                            }.getOrNull() ?: "Server error"
                            _connectionState.value = ConnectionState.Error(msg)
                            null
                        }
                        else -> null
                    }
                }.getOrNull()?.forEach { msg ->
                    scope.launch { _messages.emit(msg) }
                }
            }
        }
    }

    private fun parseMessage(msgJson: kotlinx.serialization.json.JsonObject): Message = Message(
        id = msgJson["id"]!!.jsonPrimitive.content,
        bookingId = msgJson["bookingId"]!!.jsonPrimitive.content,
        senderId = msgJson["senderId"]!!.jsonPrimitive.content,
        senderName = msgJson["senderName"]!!.jsonPrimitive.content,
        body = msgJson["body"]!!.jsonPrimitive.content,
        timestampMs = msgJson["timestampMs"]!!.jsonPrimitive.content.toLong(),
    )

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
        sendRaw("42/chat,$payload")
    }
}
