package com.parentdashboard.network

import android.content.Context
import com.parentdashboard.domain.model.P2PSessionState
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NearbyConnectionsTransport(
    context: Context,
    private val gson: Gson
) {
    private val client: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val strategy = Strategy.P2P_CLUSTER

    private val _state = MutableStateFlow(P2PSessionState.IDLE)
    val state: StateFlow<P2PSessionState> = _state.asStateFlow()

    data class NearbyEndpoint(val id: String, val name: String)

    private val _endpoints = MutableStateFlow<List<NearbyEndpoint>>(emptyList())
    val endpoints: StateFlow<List<NearbyEndpoint>> = _endpoints.asStateFlow()

    private val _incoming = MutableSharedFlow<P2PMessage>(extraBufferCapacity = 32)
    val incoming: SharedFlow<P2PMessage> = _incoming.asSharedFlow()

    private var connectedEndpointId: String? = null

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                runCatching { gson.fromJson(String(bytes), P2PMessage::class.java) }
                    .getOrNull()
                    ?.let { _incoming.tryEmit(it) }
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val connectionCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                _state.value = P2PSessionState.CONNECTED
                stopAdvertising()
                stopDiscovery()
            } else {
                _state.value = P2PSessionState.ERROR
            }
        }
        override fun onDisconnected(endpointId: String) {
            connectedEndpointId = null
            _state.value = P2PSessionState.IDLE
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val current = _endpoints.value
            if (current.none { it.id == endpointId }) {
                _endpoints.value = current + NearbyEndpoint(endpointId, info.endpointName)
            }
        }
        override fun onEndpointLost(endpointId: String) {
            _endpoints.value = _endpoints.value.filterNot { it.id == endpointId }
        }
    }

    fun startHosting() {
        _state.value = P2PSessionState.ADVERTISING
        client.startAdvertising(
            "ParentDashboard Host",
            NetworkUtils.NEARBY_SERVICE_ID,
            connectionCallback,
            AdvertisingOptions.Builder().setStrategy(strategy).build()
        ).addOnFailureListener { _state.value = P2PSessionState.ERROR }
    }

    fun startDiscovery() {
        _state.value = P2PSessionState.DISCOVERING
        _endpoints.value = emptyList()
        client.startDiscovery(
            NetworkUtils.NEARBY_SERVICE_ID,
            discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(strategy).build()
        ).addOnFailureListener { _state.value = P2PSessionState.ERROR }
    }

    fun connectTo(endpointId: String) {
        _state.value = P2PSessionState.CONNECTING
        client.requestConnection(
            "ParentDashboard Player",
            endpointId,
            connectionCallback
        ).addOnFailureListener { _state.value = P2PSessionState.ERROR }
    }

    private fun stopAdvertising() {
        client.stopAdvertising()
    }

    private fun stopDiscovery() {
        client.stopDiscovery()
    }

    suspend fun send(message: P2PMessage) {
        val endpoint = connectedEndpointId ?: return
        val bytes = gson.toJson(message).toByteArray()
        suspendCancellableCoroutine { cont ->
            client.sendPayload(endpoint, Payload.fromBytes(bytes))
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    fun disconnect() {
        connectedEndpointId?.let { client.disconnectFromEndpoint(it) }
        client.stopAllEndpoints()
        connectedEndpointId = null
        _state.value = P2PSessionState.IDLE
        _endpoints.value = emptyList()
    }

    fun isConnected(): Boolean = connectedEndpointId != null
}
