package com.parentdashboard.network

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Looper
import com.parentdashboard.domain.model.P2PSessionState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
class WifiDirectP2PTransport(
    private val context: Context,
    gson: Gson,
    private val scope: CoroutineScope
) {
    private val manager: WifiP2pManager? =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private val channel = manager?.initialize(context, Looper.getMainLooper(), null)
    private val tcp = TcpP2PConnection(gson, scope)

    private val _state = MutableStateFlow(P2PSessionState.IDLE)
    val state: StateFlow<P2PSessionState> = _state.asStateFlow()

    private val _peers = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val peers: StateFlow<List<WifiP2pDevice>> = _peers.asStateFlow()

    private val _hostHint = MutableStateFlow<String?>(null)
    val hostHint: StateFlow<String?> = _hostHint.asStateFlow()

    val incoming = tcp.incoming

    private var receiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager?.requestPeers(channel) { list: WifiP2pDeviceList ->
                        _peers.value = list.deviceList.toList()
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    manager?.requestConnectionInfo(channel) { info: WifiP2pInfo ->
                        onConnectionInfo(info)
                    }
                }
            }
        }
    }

    private fun registerReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        receiverRegistered = true
    }

    private fun onConnectionInfo(info: WifiP2pInfo) {
        if (!info.groupFormed) return
        scope.launch {
            try {
                val host = info.groupOwnerAddress?.hostAddress ?: return@launch
                if (info.isGroupOwner) {
                    _hostHint.value = host
                    withContext(Dispatchers.IO) { tcp.startServer() }
                } else {
                    tcp.connect(host)
                }
                _state.value = P2PSessionState.CONNECTED
            } catch (_: Exception) {
                _state.value = P2PSessionState.ERROR
            }
        }
    }

    fun startHosting() {
        if (manager == null || channel == null) {
            _state.value = P2PSessionState.ERROR
            return
        }
        registerReceiver()
        _state.value = P2PSessionState.ADVERTISING
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _hostHint.value = "Wi‑Fi Direct group created"
            }
            override fun onFailure(reason: Int) {
                _state.value = P2PSessionState.ERROR
            }
        })
    }

    fun startDiscovery() {
        if (manager == null || channel == null) {
            _state.value = P2PSessionState.ERROR
            return
        }
        registerReceiver()
        _state.value = P2PSessionState.DISCOVERING
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = Unit
            override fun onFailure(reason: Int) {
                _state.value = P2PSessionState.ERROR
            }
        })
    }

    fun connectTo(device: WifiP2pDevice) {
        if (manager == null || channel == null) return
        _state.value = P2PSessionState.CONNECTING
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = Unit
            override fun onFailure(reason: Int) {
                _state.value = P2PSessionState.ERROR
            }
        })
    }

    suspend fun send(message: P2PMessage) = tcp.send(message)

    fun disconnect() {
        tcp.close()
        runCatching { manager?.removeGroup(channel, null) }
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(receiver) }
            receiverRegistered = false
        }
        _state.value = P2PSessionState.IDLE
        _peers.value = emptyList()
        _hostHint.value = null
    }

    fun isConnected(): Boolean = tcp.isConnected
}
