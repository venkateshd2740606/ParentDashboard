package com.parentdashboard.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentdashboard.R
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.P2PConnectionType
import com.parentdashboard.domain.model.P2PRole
import com.parentdashboard.domain.model.P2PSessionState
import com.parentdashboard.network.NetworkUtils
import com.parentdashboard.network.P2PSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MultiplayerLobbyUiState(
    val connectionType: P2PConnectionType = P2PConnectionType.NEARBY,
    val role: P2PRole? = null,
    val sessionState: P2PSessionState = P2PSessionState.IDLE,
    val statusMessage: String? = null,
    val hostIp: String? = null,
    val joinAddress: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val peerNames: List<String> = emptyList(),
    val isReadyToPlay: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MultiplayerLobbyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val p2pSessionManager: P2PSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiplayerLobbyUiState())
    val uiState: StateFlow<MultiplayerLobbyUiState> = _uiState.asStateFlow()

    private val _navigateToGame = MutableSharedFlow<Difficulty>(extraBufferCapacity = 1)
    val navigateToGame: SharedFlow<Difficulty> = _navigateToGame.asSharedFlow()
    private var navigatedToGame = false

    init {
        viewModelScope.launch {
            combine(
                p2pSessionManager.sessionState,
                p2pSessionManager.statusMessage,
                p2pSessionManager.wifiLanHostIp,
                peerNamesFlow()
            ) { state, status, hostIp, peers ->
                Quadruple(state, status, hostIp, peers)
            }.collect { (state, status, hostIp, peers) ->
                _uiState.update {
                    it.copy(
                        sessionState = state,
                        statusMessage = status,
                        hostIp = hostIp ?: NetworkUtils.getLocalIpAddress(),
                        peerNames = peers,
                        isReadyToPlay = state == P2PSessionState.CONNECTED,
                        errorMessage = if (state == P2PSessionState.ERROR) {
                            context.getString(R.string.msg_connection_failed)
                        } else null
                    )
                }
                if (state == P2PSessionState.CONNECTED && !navigatedToGame) {
                    navigatedToGame = true
                    _navigateToGame.tryEmit(_uiState.value.difficulty)
                }
            }
        }
    }

    private fun peerNamesFlow(): Flow<List<String>> = combine(
        _uiState.map { it.connectionType }.distinctUntilChanged(),
        p2pSessionManager.wifiDirectPeers,
        p2pSessionManager.nearbyEndpoints
    ) { connectionType, wifiPeers, nearby ->
        when (connectionType) {
            P2PConnectionType.WIFI_DIRECT -> wifiPeers.map { it.deviceName.ifBlank { it.deviceAddress } }
            P2PConnectionType.NEARBY -> nearby.map { it.name }
            else -> emptyList()
        }
    }

    fun selectConnectionType(type: P2PConnectionType) {
        p2pSessionManager.disconnect()
        _uiState.update { it.copy(connectionType = type, role = null, joinAddress = "", errorMessage = null) }
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun updateJoinAddress(value: String) {
        _uiState.update { it.copy(joinAddress = value) }
    }

    fun hostGame() {
        val type = _uiState.value.connectionType
        p2pSessionManager.beginSession(type, P2PRole.HOST)
        _uiState.update { it.copy(role = P2PRole.HOST, errorMessage = null) }
        p2pSessionManager.startHosting()
    }

    fun joinGame() {
        val type = _uiState.value.connectionType
        p2pSessionManager.beginSession(type, P2PRole.CLIENT)
        _uiState.update { it.copy(role = P2PRole.CLIENT, errorMessage = null) }
        when (type) {
            P2PConnectionType.WIFI_LAN -> {
                val ip = _uiState.value.joinAddress.trim()
                if (ip.isNotBlank()) p2pSessionManager.connectWifiLan(ip)
            }
            P2PConnectionType.WIFI_DIRECT, P2PConnectionType.NEARBY -> p2pSessionManager.startDiscovery()
        }
    }

    fun connectToPeer(name: String) {
        when (_uiState.value.connectionType) {
            P2PConnectionType.WIFI_DIRECT -> {
                val device = p2pSessionManager.wifiDirectPeers.value
                    .firstOrNull { it.deviceName == name || it.deviceAddress == name }
                device?.let { p2pSessionManager.connectWifiDirect(it) }
            }
            P2PConnectionType.NEARBY -> {
                val endpoint = p2pSessionManager.nearbyEndpoints.value.firstOrNull { it.name == name }
                endpoint?.let { p2pSessionManager.connectNearby(it.id) }
            }
            else -> Unit
        }
    }

    fun disconnect() {
        p2pSessionManager.disconnect()
        navigatedToGame = false
        _uiState.update { it.copy(role = null, sessionState = P2PSessionState.IDLE) }
    }

    override fun onCleared() {
        super.onCleared()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
