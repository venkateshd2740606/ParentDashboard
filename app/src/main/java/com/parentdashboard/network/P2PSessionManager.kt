package com.parentdashboard.network

import android.content.Context
import com.parentdashboard.domain.model.P2PConnectionType
import com.parentdashboard.domain.model.P2PRole
import com.parentdashboard.domain.model.P2PSessionState
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class P2PSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var wifiLan: WifiLanP2PTransport? = null
    private var wifiDirect: WifiDirectP2PTransport? = null
    private var nearby: NearbyConnectionsTransport? = null

    private val emptyWifiPeers = MutableStateFlow<List<android.net.wifi.p2p.WifiP2pDevice>>(emptyList())
    private val emptyNearbyEndpoints = MutableStateFlow<List<NearbyConnectionsTransport.NearbyEndpoint>>(emptyList())
    private val emptyHostIp = MutableStateFlow<String?>(null)

    private val _connectionType = MutableStateFlow<P2PConnectionType?>(null)
    val connectionType: StateFlow<P2PConnectionType?> = _connectionType.asStateFlow()

    private val _role = MutableStateFlow<P2PRole?>(null)
    val role: StateFlow<P2PRole?> = _role.asStateFlow()

    private val _sessionState = MutableStateFlow(P2PSessionState.IDLE)
    val sessionState: StateFlow<P2PSessionState> = _sessionState.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _incoming = MutableSharedFlow<P2PMessage>(extraBufferCapacity = 32)
    val incoming: SharedFlow<P2PMessage> = _incoming.asSharedFlow()

    private var collectJob: kotlinx.coroutines.Job? = null

    fun beginSession(type: P2PConnectionType, role: P2PRole) {
        disconnect()
        _connectionType.value = type
        _role.value = role
        when (type) {
            P2PConnectionType.WIFI_LAN -> wifiLan = WifiLanP2PTransport(context, gson, scope)
            P2PConnectionType.WIFI_DIRECT -> wifiDirect = WifiDirectP2PTransport(context, gson, scope)
            P2PConnectionType.NEARBY -> nearby = NearbyConnectionsTransport(context, gson)
        }
        observeActiveTransport()
    }

    private fun observeActiveTransport() {
        collectJob?.cancel()
        collectJob = scope.launch {
            when (_connectionType.value) {
                P2PConnectionType.WIFI_LAN -> {
                    val t = wifiLan ?: return@launch
                    launch { t.state.collect { _sessionState.value = it } }
                    launch { t.hostAddress.collect { ip -> _statusMessage.value = ip?.let { "Host IP: $it" } } }
                    launch { t.incoming.collect { _incoming.emit(it) } }
                }
                P2PConnectionType.WIFI_DIRECT -> {
                    val t = wifiDirect ?: return@launch
                    launch { t.state.collect { _sessionState.value = it } }
                    launch { t.hostHint.collect { _statusMessage.value = it } }
                    launch { t.incoming.collect { _incoming.emit(it) } }
                }
                P2PConnectionType.NEARBY -> {
                    val t = nearby ?: return@launch
                    launch { t.state.collect { _sessionState.value = it } }
                    launch { t.incoming.collect { _incoming.emit(it) } }
                }
                null -> Unit
            }
        }
    }

    fun startHosting() {
        when (_connectionType.value) {
            P2PConnectionType.WIFI_LAN -> wifiLan?.startHosting()
            P2PConnectionType.WIFI_DIRECT -> wifiDirect?.startHosting()
            P2PConnectionType.NEARBY -> nearby?.startHosting()
            null -> Unit
        }
    }

    fun startDiscovery() {
        when (_connectionType.value) {
            P2PConnectionType.WIFI_DIRECT -> wifiDirect?.startDiscovery()
            P2PConnectionType.NEARBY -> nearby?.startDiscovery()
            else -> Unit
        }
    }

    fun connectWifiLan(hostIp: String) {
        wifiLan?.connectTo(hostIp)
    }

    fun connectWifiDirect(device: android.net.wifi.p2p.WifiP2pDevice) {
        wifiDirect?.connectTo(device)
    }

    fun connectNearby(endpointId: String) {
        nearby?.connectTo(endpointId)
    }

    val wifiDirectPeers: StateFlow<List<android.net.wifi.p2p.WifiP2pDevice>>
        get() = wifiDirect?.peers ?: emptyWifiPeers

    val nearbyEndpoints: StateFlow<List<NearbyConnectionsTransport.NearbyEndpoint>>
        get() = nearby?.endpoints ?: emptyNearbyEndpoints

    val wifiLanHostIp: StateFlow<String?>
        get() = wifiLan?.hostAddress ?: emptyHostIp

    suspend fun send(message: P2PMessage) {
        when (_connectionType.value) {
            P2PConnectionType.WIFI_LAN -> wifiLan?.send(message)
            P2PConnectionType.WIFI_DIRECT -> wifiDirect?.send(message)
            P2PConnectionType.NEARBY -> nearby?.send(message)
            null -> Unit
        }
    }

    fun isConnected(): Boolean = when (_connectionType.value) {
        P2PConnectionType.WIFI_LAN -> wifiLan?.isConnected() == true
        P2PConnectionType.WIFI_DIRECT -> wifiDirect?.isConnected() == true
        P2PConnectionType.NEARBY -> nearby?.isConnected() == true
        null -> false
    }

    fun disconnect() {
        collectJob?.cancel()
        wifiLan?.disconnect()
        wifiDirect?.disconnect()
        nearby?.disconnect()
        wifiLan = null
        wifiDirect = null
        nearby = null
        _connectionType.value = null
        _role.value = null
        _sessionState.value = P2PSessionState.IDLE
        _statusMessage.value = null
    }
}
