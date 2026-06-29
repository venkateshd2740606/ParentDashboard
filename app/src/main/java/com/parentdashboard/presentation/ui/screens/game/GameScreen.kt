package com.parentdashboard.presentation.ui.screens.game

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.ads.AdManager
import com.parentdashboard.domain.model.ChallengeType
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.engine.ParentDashboardEngine
import com.parentdashboard.presentation.ui.components.GameStatChip
import com.parentdashboard.presentation.ui.components.ParentDashboardBoard
import com.parentdashboard.presentation.viewmodel.GameLoadError
import com.parentdashboard.presentation.viewmodel.GameViewModel
import com.parentdashboard.util.FeedbackHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: Long?,
    difficulty: Difficulty?,
    tutorialIndex: Int?,
    endlessWave: Int?,
    seed: Long?,
    levelNumber: Int,
    challengeType: ChallengeType?,
    sameDevice: Boolean,
    networkP2P: Boolean = false,
    vsAi: Boolean = false,
    playerOne: String,
    playerTwo: String,
    hapticFeedback: Boolean,
    soundEnabled: Boolean,
    timerVisible: Boolean,
    onNavigateBack: () -> Unit,
    adManager: AdManager,
    reducedMotion: Boolean = false,
    viewModel: GameViewModel = hiltViewModel()
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val showWin by viewModel.showWinDialog.collectAsStateWithLifecycle()
    val winState by viewModel.winDialogState.collectAsStateWithLifecycle()
    val shareText by viewModel.shareText.collectAsStateWithLifecycle()
    val sameDeviceSession by viewModel.sameDeviceSessionState.collectAsStateWithLifecycle()
    val networkSession by viewModel.networkSessionState.collectAsStateWithLifecycle()
    val botSession by viewModel.botSessionState.collectAsStateWithLifecycle()
    val multiplayerSession = when {
        networkP2P -> networkSession
        vsAi -> botSession
        else -> sameDeviceSession
    }
    val isMultiplayer = sameDevice || networkP2P || vsAi
    val hintsRemaining by viewModel.hintsRemaining.collectAsStateWithLifecycle()
    val showNoHintsDialog by viewModel.showNoHintsDialog.collectAsStateWithLifecycle()
    val loadError by viewModel.loadError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    var paused by remember { mutableStateOf(false) }

    val sessionKey = remember(
        gameId,
        difficulty,
        tutorialIndex,
        endlessWave,
        seed,
        levelNumber,
        challengeType,
        sameDevice,
        networkP2P,
        vsAi,
        playerOne,
        playerTwo
    ) {
        listOf(
            gameId,
            difficulty?.name,
            tutorialIndex,
            endlessWave,
            seed,
            levelNumber,
            challengeType?.name,
            sameDevice,
            networkP2P,
            vsAi,
            playerOne,
            playerTwo
        ).joinToString("|")
    }

    LaunchedEffect(sessionKey) {
        viewModel.initializeSession(sessionKey) {
            when {
                gameId != null -> viewModel.loadGame(gameId)
                networkP2P -> viewModel.startNetworkP2P(difficulty ?: Difficulty.MEDIUM)
                vsAi -> viewModel.startVsAi(difficulty ?: Difficulty.MEDIUM)
                sameDevice -> viewModel.startSameDevice(playerOne, playerTwo, difficulty ?: Difficulty.MEDIUM)
                challengeType != null -> viewModel.startChallenge(challengeType)
                seed != null -> viewModel.startFromSeed(seed, levelNumber, difficulty ?: Difficulty.MEDIUM)
                tutorialIndex != null -> viewModel.startTutorial(tutorialIndex)
                endlessWave != null -> viewModel.startEndless(endlessWave)
                difficulty != null && !sameDevice && !networkP2P && !vsAi -> viewModel.startNewGame(difficulty)
            }
        }
    }

    var solvedFeedbackPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(game?.id) {
        solvedFeedbackPlayed = false
    }

    LaunchedEffect(game?.completedAt) {
        if (game?.isCompleted == true && !solvedFeedbackPlayed) {
            solvedFeedbackPlayed = true
            FeedbackHelper.onPuzzleSolved(context, hapticFeedback, soundEnabled)
        }
    }

    LaunchedEffect(game?.id, paused, isMultiplayer) {
        while (true) {
            delay(1000)
            if (paused || isMultiplayer) continue
            viewModel.tickElapsed()
        }
    }

    loadError?.let { error ->
        val message = when (error) {
            GameLoadError.GAME_NOT_FOUND -> stringResource(R.string.load_error_game_not_found)
            GameLoadError.CHALLENGE_ALREADY_COMPLETED -> stringResource(R.string.load_error_challenge_completed)
            GameLoadError.TUTORIAL_NOT_FOUND -> stringResource(R.string.load_error_tutorial_not_found)
        }
        AlertDialog(
            onDismissRequest = {
                viewModel.clearLoadError()
                onNavigateBack()
            },
            title = { Text(stringResource(R.string.load_error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLoadError()
                    onNavigateBack()
                }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    if (showWin) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissWinDialog()
                if (sameDevice) {
                    // Continue same-device match
                } else {
                    viewModel.endSameDeviceSession()
                    onNavigateBack()
                }
            },
            title = { Text(stringResource(R.string.puzzle_solved)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (isMultiplayer) {
                            stringResource(
                                R.string.same_device_round_won,
                                winState.sameDeviceLocalScore,
                                winState.sameDeviceRemoteScore
                            )
                        } else {
                            stringResource(R.string.congratulations)
                        }
                    )
                    if (!sameDevice && game != null) {
                        Text(stringResource(R.string.level_seed_display, game!!.level.seed))
                    }
                    if (winState.challengeRewardCoins > 0) {
                        Text(stringResource(R.string.reward_coins, winState.challengeRewardCoins))
                        Text(stringResource(R.string.reward_xp, winState.challengeRewardXp))
                    }
                }
            },
            confirmButton = {
                if (winState.showNextLevel && !isMultiplayer) {
                    TextButton(onClick = viewModel::startNextLevel) {
                        Text(stringResource(R.string.next_level))
                    }
                } else {
                    TextButton(onClick = {
                        viewModel.dismissWinDialog()
                        if (isMultiplayer) {
                            // stay in same-device match
                        } else {
                            activity?.let { adManager.maybeShowInterstitialAd(it) { onNavigateBack() } }
                                ?: onNavigateBack()
                        }
                    }) {
                        Text(if (isMultiplayer) stringResource(R.string.next_round) else stringResource(R.string.ok))
                    }
                }
            },
            dismissButton = when {
                shareText != null && !isMultiplayer -> {
                    {
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                            )
                        }) { Text(stringResource(R.string.share_level)) }
                    }
                }
                winState.showNextLevel && !isMultiplayer -> {
                    {
                        TextButton(onClick = {
                            viewModel.dismissWinDialog()
                            activity?.let { adManager.maybeShowInterstitialAd(it) { onNavigateBack() } }
                                ?: onNavigateBack()
                        }) { Text(stringResource(R.string.back)) }
                    }
                }
                else -> null
            }
        )
    }

    if (showNoHintsDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNoHintsDialog,
            icon = {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.no_hints_title)) },
            text = { Text(stringResource(R.string.no_hints_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissNoHintsDialog) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    val hintAvailable = game?.let { false } == true
    val outOfHints = hintsRemaining <= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isMultiplayer) stringResource(R.string.two_player)
                        else stringResource(R.string.color_sort)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.endMultiplayerSession()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!sameDevice && game != null) {
                        IconButton(onClick = {
                            val text = com.parentdashboard.engine.ParentDashboardGenerator.formatShareText(
                                game!!.level.seed,
                                game!!.level.levelNumber,
                                game!!.level.difficulty
                            )
                            context.startActivity(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                            )
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_level))
                        }
                    }
                    if (!isMultiplayer) {
                        IconButton(onClick = { paused = !paused }) {
                            Icon(Icons.Default.Pause, contentDescription = stringResource(R.string.pause))
                        }
                        IconButton(
                            onClick = { viewModel.requestHint() },
                            enabled = hintAvailable
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = stringResource(R.string.hint),
                                tint = if (!hintAvailable) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (loadError != null) {
            Box(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        game?.let { g ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (isMultiplayer && multiplayerSession != null) {
                    Text(
                        text = stringResource(
                            R.string.same_device_turn,
                            multiplayerSession!!.activePlayerName,
                            multiplayerSession!!.localScore,
                            multiplayerSession!!.remoteScore
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!sameDevice && timerVisible) {
                        GameStatChip(stringResource(R.string.time), g.completionTimeFormatted)
                    }
                    GameStatChip(stringResource(R.string.moves), g.moves.toString())
                    if (!isMultiplayer) {
                        GameStatChip(
                            stringResource(R.string.hints),
                            stringResource(R.string.hints_remaining, hintsRemaining)
                        )
                        GameStatChip(stringResource(R.string.level_seed), g.level.seed.toString())
                    }
                }
                if (paused && !isMultiplayer) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.game_paused), style = MaterialTheme.typography.headlineMedium)
                    }
                } else {
                    ParentDashboardBoard(
                        game = g,
                        reducedMotion = reducedMotion,
                        onNextStep = viewModel::onNextStep,
                        onAddChild = viewModel::onAddChild,
                        onSelectChild = viewModel::onSelectChild,
                        onLogProgress = viewModel::onLogProgress,
                        onViewWeeklyReport = viewModel::onViewWeeklyReport,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
