package com.connecto.store.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class StoreRealtimeEvent {
    object Connected : StoreRealtimeEvent()
    data class AppUpdate(val packageName: String, val versionName: String, val versionCode: Int) : StoreRealtimeEvent()
    data class CatalogSync(val timestamp: Long) : StoreRealtimeEvent()
    object Disconnected : StoreRealtimeEvent()
}

/**
 * Real-Time Edge Broadcast Client for Our Store.
 * Connects to Cloudflare Edge WebSockets mesh to receive instantaneous
 * app release, catalog update, and in-app self-update broadcasts.
 */
object StoreRealtimeManager {

    private const val TAG = "OurStoreRealtime"
    private const val WS_ENDPOINT = "wss://connecto.fun/api/v1/store/ws?client=our_store&channel=store_updates"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var isIntentionalClose = false
    private var reconnectDelayMs = 2000L

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _events = MutableSharedFlow<StoreRealtimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<StoreRealtimeEvent> = _events.asSharedFlow()

    private val client by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // Indefinite for WebSockets
            .pingInterval(20, TimeUnit.SECONDS)   // Automatic RFC-6455 Ping frame every 20s
            .retryOnConnectionFailure(true)
            .build()
    }

    @Synchronized
    fun start() {
        if (_isConnected.value || webSocket != null) return
        isIntentionalClose = false
        connect()
    }

    @Synchronized
    fun stop() {
        isIntentionalClose = true
        reconnectJob?.cancel()
        reconnectJob = null
        try {
            webSocket?.close(1000, "Store client closed")
        } catch (_: Exception) {}
        webSocket = null
        _isConnected.value = false
    }

    private fun connect() {
        if (isIntentionalClose) return

        val request = Request.Builder()
            .url(WS_ENDPOINT)
            .addHeader("User-Agent", "OurStore-Client/1.9.9")
            .build()

        Log.i(TAG, "Initiating real-time edge WebSocket connection to: $WS_ENDPOINT")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i(TAG, "🟢 Real-time edge WebSocket connected! Status: ${response.code}")
                _isConnected.value = true
                reconnectDelayMs = 2000L // Reset backoff on successful handshake
                scope.launch {
                    _events.emit(StoreRealtimeEvent.Connected)
                }

                // Subscribe to real-time store updates channel
                val subMsg = JSONObject().apply {
                    put("action", "subscribe")
                    put("channel", "store_updates")
                    put("client", "our_store")
                }
                ws.send(subMsg.toString())
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "📥 Real-time message received: $text")
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    when (type) {
                        "app_update" -> {
                            val pkg = json.optString("package_name")
                            val verName = json.optString("version_name")
                            val verCode = json.optInt("version_code")
                            Log.i(TAG, "⚡ Real-time APP_UPDATE received: $pkg -> v$verName ($verCode)")
                            scope.launch {
                                _events.emit(StoreRealtimeEvent.AppUpdate(pkg, verName, verCode))
                            }
                        }
                        "catalog_update", "catalog_sync", "release_published" -> {
                            val ts = json.optLong("timestamp", System.currentTimeMillis())
                            Log.i(TAG, "⚡ Real-time CATALOG_SYNC received from edge")
                            scope.launch {
                                _events.emit(StoreRealtimeEvent.CatalogSync(ts))
                            }
                        }
                        "connected" -> {
                            Log.d(TAG, "Handshake confirmed by edge server: ${json.optString("message")}")
                        }
                        "pong" -> {
                            Log.v(TAG, "Received heartbeat pong")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse WebSocket message", e)
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code / $reason")
                _isConnected.value = false
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code / $reason")
                _isConnected.value = false
                scope.launch { _events.emit(StoreRealtimeEvent.Disconnected) }
                scheduleReconnect()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "⚠️ Real-time WebSocket connection failed: ${t.message}")
                _isConnected.value = false
                webSocket = null
                scope.launch { _events.emit(StoreRealtimeEvent.Disconnected) }
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (isIntentionalClose) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            Log.d(TAG, "Scheduling real-time reconnect in ${reconnectDelayMs}ms...")
            delay(reconnectDelayMs)
            reconnectDelayMs = (reconnectDelayMs * 1.5).toLong().coerceAtMost(30000L) // Exponential backoff capped at 30s
            connect()
        }
    }

    /**
     * Sends an instantaneous check_updates request over active WebSocket.
     * Returns true if request was dispatched over WebSocket, false if disconnected.
     */
    fun requestCheckUpdates(): Boolean {
        val ws = webSocket
        if (ws != null && _isConnected.value) {
            val pingMsg = JSONObject().apply {
                put("action", "check_updates")
                put("timestamp", System.currentTimeMillis())
            }
            return ws.send(pingMsg.toString())
        }
        return false
    }
}
