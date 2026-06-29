package com.parentdashboard.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parentdashboard.domain.model.PuzzleArchetype
import com.parentdashboard.domain.model.SkillCategory

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seed: Long,
    val levelNumber: Int,
    val difficulty: String,
    val status: String,
    val tubeStateJson: String,
    val selectedTubeId: Int,
    val moves: Int,
    val hintsUsed: Int,
    val elapsedSeconds: Long,
    val createdAt: Long,
    val lastPlayedAt: Long,
    val completedAt: Long?,
    val isTutorial: Boolean,
    val isEndless: Boolean,
    val challengeType: String?,
    val challengeKey: String?,
    val levelJson: String,
    val coinsEarned: Int,
    val xpEarned: Int
)

@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey val id: Int = 1,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val gamesAbandoned: Int,
    val totalPlayTimeSeconds: Long,
    val fastestTimeBeginner: Long,
    val fastestTimeEasy: Long,
    val fastestTimeMedium: Long,
    val fastestTimeHard: Long,
    val fastestTimeExpert: Long,
    val fastestTimeMaster: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastPlayedDate: String,
    val xpPoints: Long,
    val level: Int,
    val hintsUsedTotal: Int,
    val perfectGames: Int,
    val poursTotal: Int,
    val endlessHighScore: Int
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val progress: Int
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val key: String,
    val type: String,
    val seed: Long,
    val difficulty: String,
    val isCompleted: Boolean,
    val completionTime: Long?,
    val moves: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val streakDay: Int
)

@Entity(tableName = "economy")
data class EconomyEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int,
    val totalCoinsEarned: Int,
    val totalCoinsSpent: Int,
    val unlockedThemes: String
)

@Entity(tableName = "puzzle_profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val gamesAnalyzed: Int = 0,
    val totalSolveTimeSeconds: Long = 0,
    val totalMoves: Int = 0,
    val totalOptimalMoves: Int = 0,
    val totalHintsUsed: Int = 0,
    val fastCompletions: Int = 0,
    val slowCompletions: Int = 0,
    val perfectCompletions: Int = 0,
    val complexChainWins: Int = 0,
    val inefficientWins: Int = 0,
    val hintHeavyWins: Int = 0,
    val archetype: String = PuzzleArchetype.EXPLORER.name,
    val strength: String = SkillCategory.PATTERN_RECOGNITION.name,
    val weakness: String = SkillCategory.TIME_PRESSURE.name,
    val adaptiveColorModifier: Int = 0
)
