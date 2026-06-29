package com.parentdashboard.network

import android.content.Context
import com.parentdashboard.domain.model.P2PSessionState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiLanP2PTransport(
    context: Context,
    gson: Gson,
    private val scope: CoroutineScope
) {
    private val tcp = TcpP2PConnection(gson, scope)
    private val _state = MutableStateFlow(P2PSessionState.IDLE)
    val state: StateFlow<P2PSessionState> = _state.asStateFlow()

    private val _hostAddress = MutableStateFlow<String?>(null)
    val hostAddress: StateFlow<String?> = _hostAddress.asStateFlow()

    val incoming = tcp.incoming

    fun startHosting() {
        _state.value = P2PSessionState.ADVERTISING
        _hostAddress.value = NetworkUtils.getLocalIpAddress()
        tcp.listenForClient(
            onConnected = { _state.value = P2PSessionState.CONNECTED },
            onError = { _state.value = P2PSessionState.ERROR }
        )
    }

    fun connectTo(host: String) {
        scope.launch {
            try {
                _state.value = P2PSessionState.CONNECTING
                tcp.connect(host.trim())
                _state.value = P2PSessionState.CONNECTED
            } catch (e: Exception) {
                _state.value = P2PSessionState.ERROR
            }
        }
    }

    suspend fun send(message: P2PMessage) = tcp.send(message)

    fun disconnect() {
        tcp.close()
        _state.value = P2PSessionState.IDLE
        _hostAddress.value = null
    }

    fun isConnected(): Boolean = tcp.isConnected
}
