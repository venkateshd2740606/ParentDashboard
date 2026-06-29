package com.parentdashboard.network

import com.parentdashboard.domain.model.GameStatus

/**
 * Generic local P2P message envelope for puzzle games (level sync, moves, game state).
 */
data class P2PMessage(
    val type: String,
    val levelSeed: Long? = null,
    val levelNumber: Int? = null,
    val movePayload: String? = null,
    val playerName: String? = null,
    val gameStatus: String? = null,
    val difficulty: String? = null
) {
    companion object {
        const val TYPE_GAME_START = "game_start"
        const val TYPE_MOVE = "move"
        const val TYPE_SYNC = "sync"
        const val TYPE_RESIGN = "resign"

        fun gameStart(levelSeed: Long, levelNumber: Int, hostName: String, difficulty: String) = P2PMessage(
            type = TYPE_GAME_START,
            levelSeed = levelSeed,
            levelNumber = levelNumber,
            playerName = hostName,
            difficulty = difficulty
        )

        fun move(payload: String) = P2PMessage(type = TYPE_MOVE, movePayload = payload)

        fun sync(payload: String) = P2PMessage(type = TYPE_SYNC, movePayload = payload)

        fun resign() = P2PMessage(type = TYPE_RESIGN)

        fun gameStatus(status: GameStatus) = P2PMessage(
            type = TYPE_SYNC,
            gameStatus = status.name
        )
    }
}
