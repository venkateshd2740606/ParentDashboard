package com.parentdashboard.domain.repository

import com.parentdashboard.domain.model.Achievement
import com.parentdashboard.domain.model.ChallengeRecord
import com.parentdashboard.domain.model.ChallengeType
import com.parentdashboard.domain.model.ParentDashboardGame
import com.parentdashboard.domain.model.ParentDashboardLevel
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.EconomyState
import com.parentdashboard.domain.model.PuzzleProfile
import com.parentdashboard.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun createNewGame(difficulty: Difficulty, levelNumber: Int): ParentDashboardGame
    suspend fun createGameFromSeed(seed: Long, levelNumber: Int, difficulty: Difficulty): ParentDashboardGame
    suspend fun createTutorialGame(tutorialIndex: Int): ParentDashboardGame?
    suspend fun createEndlessGame(wave: Int): ParentDashboardGame
    suspend fun saveGame(game: ParentDashboardGame): Long
    suspend fun getGame(gameId: Long): ParentDashboardGame?
    suspend fun getInProgressGame(): ParentDashboardGame?
    fun observeInProgressGame(): Flow<ParentDashboardGame?>
    suspend fun completeGame(game: ParentDashboardGame): ParentDashboardGame
    suspend fun abandonGame(gameId: Long)
    suspend fun getLevel(seed: Long, levelNumber: Int, difficulty: Difficulty): ParentDashboardLevel
}

interface ChallengeRepository {
    suspend fun getChallenge(type: ChallengeType, key: String): ChallengeRecord?
    suspend fun createChallenge(type: ChallengeType, key: String, difficulty: Difficulty): ChallengeRecord
    suspend fun resolveActiveChallenge(type: ChallengeType): ChallengeRecord
    fun observeActiveChallenge(type: ChallengeType): Flow<ChallengeRecord?>
    suspend fun completeChallenge(record: ChallengeRecord, timeSeconds: Long, moves: Int): ChallengeRecord
    fun observeChallengeHistory(type: ChallengeType): Flow<List<ChallengeRecord>>
    suspend fun getCurrentStreak(type: ChallengeType): Int
    suspend fun getChallengeGame(record: ChallengeRecord): ParentDashboardGame
}

interface ProgressionRepository {
    fun observeStats(): Flow<UserStats>
    suspend fun getStats(): UserStats
    suspend fun updateStatsAfterGame(game: ParentDashboardGame)
    suspend fun grantChallengeRewards(rewardCoins: Int, rewardXp: Int)
    fun observePuzzleProfile(): Flow<PuzzleProfile>
    suspend fun getPuzzleProfile(): PuzzleProfile
    fun observeAchievements(): Flow<List<Achievement>>
    suspend fun checkAndUnlockAchievements(
        game: ParentDashboardGame,
        sameDevicePlayed: Boolean = false
    ): List<Achievement>
    fun observeEconomy(): Flow<EconomyState>
    suspend fun getEconomy(): EconomyState
    suspend fun spendCoins(amount: Int): Boolean
    suspend fun earnCoins(amount: Int)
    suspend fun unlockTheme(themeId: String): Boolean
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<com.parentdashboard.domain.model.UserPreferences>
    suspend fun updatePreferences(transform: (com.parentdashboard.domain.model.UserPreferences) -> com.parentdashboard.domain.model.UserPreferences)
    suspend fun getCampaignLevel(difficulty: Difficulty): Int
    suspend fun advanceCampaignLevel(difficulty: Difficulty): Int
}
