package com.parentdashboard.multiplayer

import com.parentdashboard.domain.model.ParentDashboardGame
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.DashboardStepMode
import com.parentdashboard.domain.model.LearningSubject
import com.parentdashboard.domain.model.MultiplayerMode
import com.parentdashboard.domain.model.MultiplayerSession
import com.parentdashboard.engine.ParentDashboardEngine
import com.parentdashboard.engine.ParentDashboardGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleBotSession @Inject constructor() {
    private val _session = MutableStateFlow<MultiplayerSession?>(null)
    val session: StateFlow<MultiplayerSession?> = _session.asStateFlow()

    private var playerGame: ParentDashboardGame? = null
    private var botGame: ParentDashboardGame? = null
    private var playerName = "You"
    private val botName = "AI Bot"

    fun start(player: String, difficulty: Difficulty, seed: Long = System.currentTimeMillis()) {
        playerName = player
        val level = ParentDashboardGenerator.generate(seed, 1, difficulty)
        val game = ParentDashboardEngine.createInitialGame(level)
        playerGame = game
        botGame = game
        _session.value = MultiplayerSession(
            mode = MultiplayerMode.SAME_DEVICE,
            localPlayerName = playerName,
            remotePlayerName = botName,
            activePlayerName = playerName,
            isActive = true,
            seed = seed,
            difficulty = difficulty
        )
    }

    fun getPlayerGame(): ParentDashboardGame? = playerGame

    fun applyPlayerAction(game: ParentDashboardGame, action: (ParentDashboardGame) -> ParentDashboardGame): ParentDashboardGame? {
        val current = playerGame ?: return null
        if (current != game) return null
        val updated = action(current)
        playerGame = updated
        botGame = updated
        return updated
    }

    fun applyBotMove(): ParentDashboardGame? {
        var game = botGame ?: return null
        game = when (game.currentStep) {
            DashboardStepMode.INTRO -> ParentDashboardEngine.nextStep(game)
            DashboardStepMode.ACTION -> {
                var working = game
                if (working.selectedChildId == null && working.children.isNotEmpty()) {
                    working = ParentDashboardEngine.selectChild(working, working.children.first().id)
                }
                if (!ParentDashboardEngine.isTaskComplete(working)) {
                    working = ParentDashboardEngine.logProgress(working, LearningSubject.ABC, 10, 1)
                }
                working
            }
            DashboardStepMode.REVIEW -> ParentDashboardEngine.nextStep(game)
            null -> game
        }
        playerGame = game
        botGame = game
        val session = _session.value
        if (session != null && game.isCompleted) {
            _session.value = session.copy(
                remoteScore = session.remoteScore + 1,
                activePlayerName = playerName
            )
        }
        return game
    }

    fun onPlayerWon() {
        val session = _session.value ?: return
        _session.value = session.copy(
            localScore = session.localScore + 1,
            activePlayerName = playerName
        )
        startNewRound(session)
    }

    fun onBotWon() {
        val session = _session.value ?: return
        _session.value = session.copy(
            remoteScore = session.remoteScore + 1,
            activePlayerName = playerName
        )
        startNewRound(session)
    }

    private fun startNewRound(session: MultiplayerSession) {
        val newSeed = session.seed + session.localScore + session.remoteScore
        val level = ParentDashboardGenerator.generate(newSeed, session.localScore + session.remoteScore + 1, session.difficulty)
        val game = ParentDashboardEngine.createInitialGame(level)
        playerGame = game
        botGame = game
    }

    fun end() {
        _session.value = null
        playerGame = null
        botGame = null
    }
}
