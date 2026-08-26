package app.voicecloud.core.realtime

import app.voicecloud.core.security.TokenVault
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URI

sealed interface RealtimeConnectionState {
    data object Disconnected : RealtimeConnectionState
    data object Connecting : RealtimeConnectionState
    data object TransportConnected : RealtimeConnectionState
    data class Authenticated(val socketId: String?) : RealtimeConnectionState
    data class Failed(val message: String) : RealtimeConnectionState
}

interface RealtimeClient {
    val state: StateFlow<RealtimeConnectionState>
    fun connect(): Boolean
    fun disconnect()
    fun emit(event: String, payload: JSONObject = JSONObject())
    fun on(event: String, listener: (Array<out Any>) -> Unit)
    fun off(event: String)
}

object RealtimeClientFactory {
    fun create(socketBaseUrl: String, tokenVault: TokenVault): RealtimeClient =
        SocketIoRealtimeClient(socketBaseUrl, tokenVault)
}

private class SocketIoRealtimeClient(
    socketBaseUrl: String,
    private val tokenVault: TokenVault,
) : RealtimeClient {
    private val connectionState = MutableStateFlow<RealtimeConnectionState>(RealtimeConnectionState.Disconnected)
    override val state: StateFlow<RealtimeConnectionState> = connectionState.asStateFlow()

    private val socketUri = URI.create(socketBaseUrl.trimEnd('/') + "/realtime")
    private var socket: Socket? = null

    override fun connect(): Boolean {
        val token = tokenVault.accessToken()?.trim().orEmpty()
        if (token.isEmpty()) {
            connectionState.value = RealtimeConnectionState.Failed("Authenticated socket user required")
            return false
        }
        if (socket?.connected() == true) return true

        connectionState.value = RealtimeConnectionState.Connecting
        val options = IO.Options().apply {
            path = "/socket.io"
            transports = arrayOf("websocket", "polling")
            reconnection = true
            reconnectionAttempts = 8
            reconnectionDelay = 700
            reconnectionDelayMax = 5_000
            timeout = 8_000
            auth = mapOf("token" to "Bearer $token")
        }
        val active = IO.socket(socketUri, options)
        socket = active
        active.on(Socket.EVENT_CONNECT) {
            connectionState.value = RealtimeConnectionState.TransportConnected
        }
        active.on("connection_established") { args ->
            val socketId = (args.firstOrNull() as? JSONObject)?.optString("socketId")?.takeIf { it.isNotBlank() }
            connectionState.value = RealtimeConnectionState.Authenticated(socketId)
        }
        active.on("auth_error") { args ->
            val message = (args.firstOrNull() as? JSONObject)?.optString("message")?.takeIf { it.isNotBlank() }
                ?: "Realtime authentication failed"
            connectionState.value = RealtimeConnectionState.Failed(message)
        }
        active.on(Socket.EVENT_CONNECT_ERROR) { args ->
            connectionState.value = RealtimeConnectionState.Failed(args.firstOrNull()?.toString() ?: "Realtime connection failed")
        }
        active.on(Socket.EVENT_DISCONNECT) {
            connectionState.value = RealtimeConnectionState.Disconnected
        }
        active.connect()
        return true
    }

    override fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
        connectionState.value = RealtimeConnectionState.Disconnected
    }

    override fun emit(event: String, payload: JSONObject) {
        check(state.value is RealtimeConnectionState.Authenticated) { "Realtime socket is not authenticated" }
        socket?.emit(event, payload)
    }

    override fun on(event: String, listener: (Array<out Any>) -> Unit) {
        socket?.on(event) { args -> listener(args) }
    }

    override fun off(event: String) {
        socket?.off(event)
    }
}
