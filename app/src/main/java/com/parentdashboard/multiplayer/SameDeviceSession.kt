package com.parentdashboard.multiplayer

import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.MultiplayerMode
import com.parentdashboard.domain.model.MultiplayerSession
import com.parentdashboard.domain.model.ParentDashboardGame
import com.parentdashboard.engine.ParentDashboardEngine
import com.parentdashboard.engine.ParentDashboardGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SameDeviceSession @Inject constructor() {
    private val _session = MutableStateFlow<MultiplayerSession?>(null)
    val session: StateFlow<MultiplayerSession?> = _session.asStateFlow()

    private var sharedGame: ParentDashboardGame? = null
    private var activePlayer = 1
    private var playerOneName = "Player 1"
    private var playerTwoName = "Player 2"

    fun start(playerOne: String, playerTwo: String, difficulty: Difficulty, seed: Long = System.currentTimeMillis()) {
        playerOneName = playerOne
        playerTwoName = playerTwo
        val level = ParentDashboardGenerator.generate(seed, 1, difficulty)
        sharedGame = ParentDashboardEngine.createInitialGame(level)
        activePlayer = 1
        publishSession(difficulty, seed, isActive = true)
    }

    fun getActiveGame(): ParentDashboardGame? = sharedGame

    fun applyAction(game: ParentDashboardGame, action: (ParentDashboardGame) -> ParentDashboardGame): ParentDashboardGame? {
        val current = sharedGame ?: return null
        if (current != game) return null
        val updated = action(current)
        if (updated == current) return updated

        if (updated.isCompleted) {
            val session = _session.value ?: return updated
            val roundWinnerIsPlayerOne = activePlayer == 1
            val newLocalScore = session.localScore + if (roundWinnerIsPlayerOne) 1 else 0
            val newRemoteScore = session.remoteScore + if (roundWinnerIsPlayerOne) 0 else 1
            val newLevel = ParentDashboardGenerator.generate(
                session.seed + newLocalScore + newRemoteScore,
                newLocalScore + newRemoteScore + 1,
                session.difficulty
            )
            sharedGame = ParentDashboardEngine.createInitialGame(newLevel)
            activePlayer = if (roundWinnerIsPlayerOne) 2 else 1
            _session.value = session.copy(
                localScore = newLocalScore,
                remoteScore = newRemoteScore,
                activePlayerName = if (activePlayer == 1) playerOneName else playerTwoName
            )
            return updated
        }

        sharedGame = updated
        activePlayer = if (activePlayer == 1) 2 else 1
        publishSession(
            difficulty = _session.value?.difficulty ?: Difficulty.MEDIUM,
            seed = _session.value?.seed ?: System.currentTimeMillis(),
            isActive = true
        )
        return updated
    }

    fun end() {
        _session.value = null
        sharedGame = null
        activePlayer = 1
    }

    private fun publishSession(difficulty: Difficulty, seed: Long, isActive: Boolean) {
        _session.value = MultiplayerSession(
            mode = MultiplayerMode.SAME_DEVICE,
            localPlayerName = playerOneName,
            remotePlayerName = playerTwoName,
            activePlayerName = if (activePlayer == 1) playerOneName else playerTwoName,
            localScore = _session.value?.localScore ?: 0,
            remoteScore = _session.value?.remoteScore ?: 0,
            isActive = isActive,
            seed = seed,
            difficulty = difficulty
        )
    }
}
