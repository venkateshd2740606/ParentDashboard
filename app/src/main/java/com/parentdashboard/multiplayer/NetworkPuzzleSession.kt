package com.parentdashboard.multiplayer

import com.parentdashboard.domain.model.ParentDashboardGame
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.MultiplayerMode
import com.parentdashboard.domain.model.MultiplayerSession
import com.parentdashboard.domain.model.P2PRole
import com.parentdashboard.engine.ParentDashboardEngine
import com.parentdashboard.engine.ParentDashboardGenerator
import com.parentdashboard.network.P2PMessage
import com.parentdashboard.network.P2PSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkPuzzleSession @Inject constructor(
    private val p2pSessionManager: P2PSessionManager
) {
    private val _session = MutableStateFlow<MultiplayerSession?>(null)
    val session: StateFlow<MultiplayerSession?> = _session.asStateFlow()

    private var sharedGame: ParentDashboardGame? = null
    private var localName = "Player 1"
    private var remoteName = "Player 2"
    private var isMyTurn = false

    val isLocalTurn: Boolean get() = isMyTurn

    suspend fun startAsHost(localPlayer: String, remotePlayer: String, difficulty: Difficulty) {
        localName = localPlayer
        remoteName = remotePlayer
        val seed = System.currentTimeMillis()
        val level = ParentDashboardGenerator.generate(seed, 1, difficulty)
        val game = ParentDashboardEngine.createInitialGame(level)
        sharedGame = game
        isMyTurn = true
        publishSession(difficulty, seed, isActive = true)
        p2pSessionManager.send(
            P2PMessage.gameStart(
                levelSeed = seed,
                levelNumber = 1,
                hostName = localName,
                difficulty = difficulty.name
            )
        )
    }

    fun getGame(): ParentDashboardGame? = sharedGame

    suspend fun applyLocalAction(action: (ParentDashboardGame) -> ParentDashboardGame): ParentDashboardGame? {
        if (!isMyTurn) return null
        val game = sharedGame ?: return null
        val updated = action(game)
        if (updated == game) return updated
        sharedGame = updated
        isMyTurn = false
        publishSession(updated.level.difficulty, updated.level.seed, isActive = true)
        p2pSessionManager.send(P2PMessage.move(encodeMove(updated, game)))
        return updated
    }

    private fun encodeMove(updated: ParentDashboardGame, previous: ParentDashboardGame): String = when {
        updated.currentStepIndex > previous.currentStepIndex -> "next"
        updated.selectedChildId != null && updated.selectedChildId != previous.selectedChildId ->
            "select:${updated.selectedChildId}"
        else -> "sync"
    }

    suspend fun onRemoteMessage(message: P2PMessage): ParentDashboardGame? {
        return when (message.type) {
            P2PMessage.TYPE_GAME_START -> {
                val seed = message.levelSeed ?: return null
                val levelNumber = message.levelNumber ?: 1
                val difficulty = message.difficulty?.let {
                    runCatching { Difficulty.valueOf(it) }.getOrNull()
                } ?: Difficulty.MEDIUM
                val level = ParentDashboardGenerator.generate(seed, levelNumber, difficulty)
                val game = ParentDashboardEngine.createInitialGame(level)
                sharedGame = game
                isMyTurn = false
                remoteName = message.playerName ?: remoteName
                publishSession(difficulty, seed, isActive = true)
                game
            }
            P2PMessage.TYPE_MOVE -> {
                if (isMyTurn) return sharedGame
                val payload = message.movePayload ?: return null
                val game = sharedGame ?: return null
                val updated = ParentDashboardEngine.applyRemoteMove(game, payload)
                sharedGame = updated
                isMyTurn = true
                publishSession(updated.level.difficulty, updated.level.seed, isActive = true)
                updated
            }
            P2PMessage.TYPE_RESIGN -> {
                isMyTurn = false
                sharedGame
            }
            else -> sharedGame
        }
    }

    suspend fun resign() {
        p2pSessionManager.send(P2PMessage.resign())
    }

    fun onRoundWon(localWon: Boolean) {
        val session = _session.value ?: return
        val newLocal = session.localScore + if (localWon) 1 else 0
        val newRemote = session.remoteScore + if (localWon) 0 else 1
        val newLevel = ParentDashboardGenerator.generate(
            session.seed + newLocal + newRemote,
            newLocal + newRemote + 1,
            session.difficulty
        )
        val newGame = ParentDashboardEngine.createInitialGame(newLevel)
        sharedGame = newGame
        isMyTurn = if (localWon) {
            p2pSessionManager.role.value != P2PRole.HOST
        } else {
            p2pSessionManager.role.value == P2PRole.HOST
        }
        _session.value = session.copy(
            localScore = newLocal,
            remoteScore = newRemote,
            activePlayerName = if (isMyTurn) localName else remoteName
        )
    }

    fun end() {
        _session.value = null
        sharedGame = null
        isMyTurn = false
    }

    private fun publishSession(difficulty: Difficulty, seed: Long, isActive: Boolean) {
        _session.value = MultiplayerSession(
            mode = MultiplayerMode.LOCAL_P2P,
            localPlayerName = localName,
            remotePlayerName = remoteName,
            activePlayerName = if (isMyTurn) localName else remoteName,
            localScore = _session.value?.localScore ?: 0,
            remoteScore = _session.value?.remoteScore ?: 0,
            isActive = isActive,
            seed = seed,
            difficulty = difficulty
        )
    }
}
