package com.parentdashboard.presentation.ui.screens.multiplayer

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.P2PConnectionType
import com.parentdashboard.domain.model.P2PSessionState
import com.parentdashboard.network.P2PPermissions
import com.parentdashboard.presentation.ui.util.displayName
import com.parentdashboard.presentation.ui.util.localizedName
import com.parentdashboard.presentation.viewmodel.MultiplayerLobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerLobbyScreen(
    onNavigateBack: () -> Unit,
    onStartGame: (Difficulty) -> Unit,
    viewModel: MultiplayerLobbyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) pendingAction?.invoke()
        pendingAction = null
    }

    fun runWithPermissions(action: () -> Unit) {
        val perms = P2PPermissions.requiredFor(state.connectionType)
        if (perms.isEmpty()) action() else {
            pendingAction = action
            permissionLauncher.launch(perms)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToGame.collect { difficulty -> onStartGame(difficulty) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.two_device_play)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.disconnect()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.choose_connection), fontWeight = FontWeight.SemiBold)
            P2PConnectionType.entries.forEach { type ->
                FilterChip(
                    selected = state.connectionType == type,
                    onClick = { viewModel.selectConnectionType(type) },
                    label = { Text(type.displayName()) },
                    leadingIcon = {
                        Icon(
                            when (type) {
                                P2PConnectionType.NEARBY -> Icons.Default.NearMe
                                P2PConnectionType.WIFI_LAN -> Icons.Default.Wifi
                                P2PConnectionType.WIFI_DIRECT -> Icons.Default.WifiTethering
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            Text(
                when (state.connectionType) {
                    P2PConnectionType.NEARBY -> stringResource(R.string.p2p_nearby_desc)
                    P2PConnectionType.WIFI_LAN -> stringResource(R.string.p2p_wifi_lan_desc)
                    P2PConnectionType.WIFI_DIRECT -> stringResource(R.string.p2p_wifi_direct_desc)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text(stringResource(R.string.choose_difficulty), fontWeight = FontWeight.SemiBold)
            Difficulty.entries.filter { it != Difficulty.ENDLESS && it != Difficulty.BEGINNER }.forEach { difficulty ->
                FilterChip(
                    selected = state.difficulty == difficulty,
                    onClick = { viewModel.selectDifficulty(difficulty) },
                    label = { Text(difficulty.localizedName()) }
                )
            }

            HorizontalDivider()

            if (state.sessionState == P2PSessionState.IDLE || state.sessionState == P2PSessionState.ERROR) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { runWithPermissions { viewModel.hostGame() } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.host_game))
                    }
                    OutlinedButton(
                        onClick = { runWithPermissions { viewModel.joinGame() } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.join_game))
                    }
                }
            }

            if (state.connectionType == P2PConnectionType.WIFI_LAN) {
                OutlinedTextField(
                    value = state.joinAddress,
                    onValueChange = viewModel::updateJoinAddress,
                    label = { Text(stringResource(R.string.host_ip_address)) },
                    placeholder = { Text("192.168.1.10") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            when (state.sessionState) {
                P2PSessionState.ADVERTISING -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                            Text(stringResource(R.string.waiting_for_opponent), fontWeight = FontWeight.Bold)
                            state.hostIp?.let {
                                Text(stringResource(R.string.share_host_ip, it), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                P2PSessionState.DISCOVERING -> {
                    Text(stringResource(R.string.searching_peers), fontWeight = FontWeight.SemiBold)
                    state.peerNames.forEach { peer ->
                        OutlinedButton(
                            onClick = { viewModel.connectToPeer(peer) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(peer) }
                    }
                    if (state.peerNames.isEmpty()) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
                P2PSessionState.CONNECTING -> {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.connecting))
                }
                P2PSessionState.CONNECTED -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Text(
                            stringResource(R.string.opponent_connected),
                            Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                P2PSessionState.ERROR -> {
                    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { viewModel.disconnect() }) {
                        Text(stringResource(R.string.try_again))
                    }
                }
                P2PSessionState.IDLE -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSameDevice: () -> Unit,
    onNavigateToTwoDevice: () -> Unit,
    onNavigateToVsAi: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.multiplayer)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.multiplayer_hub_desc), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(onClick = onNavigateToSameDevice, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.two_player))
            }
            Button(onClick = onNavigateToTwoDevice, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Devices, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.two_device_play))
            }
            OutlinedButton(onClick = onNavigateToVsAi, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.SmartToy, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.play_vs_ai))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VsAiScreen(
    onNavigateBack: () -> Unit,
    onStartGame: (Difficulty) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.play_vs_ai)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.play_vs_ai_desc), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.choose_difficulty), fontWeight = FontWeight.SemiBold)
            Difficulty.entries.filter { it != Difficulty.ENDLESS && it != Difficulty.BEGINNER }.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { selectedDifficulty = difficulty },
                    label = { Text(difficulty.localizedName()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = { onStartGame(selectedDifficulty) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.start_game))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SameDeviceScreen(
    onNavigateBack: () -> Unit,
    onStartGame: (playerOne: String, playerTwo: String, difficulty: Difficulty) -> Unit
) {
    var player1 by remember { mutableStateOf("") }
    var player2 by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    val defaultPlayerOne = stringResource(R.string.player_one)
    val defaultPlayerTwo = stringResource(R.string.player_two)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.two_player)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Text(stringResource(R.string.same_device_desc))
                }
            }
            OutlinedTextField(
                value = player1,
                onValueChange = { player1 = it },
                label = { Text(stringResource(R.string.player_one)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = player2,
                onValueChange = { player2 = it },
                label = { Text(stringResource(R.string.player_two)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.choose_difficulty))
            Difficulty.entries.filter { it != Difficulty.ENDLESS && it != Difficulty.BEGINNER }.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { selectedDifficulty = difficulty },
                    label = { Text(difficulty.localizedName()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = {
                    val p1 = player1.trim().ifBlank { defaultPlayerOne }
                    val p2 = player2.trim().ifBlank { defaultPlayerTwo }
                    onStartGame(p1, p2, selectedDifficulty)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.start_game))
            }
        }
    }
}
