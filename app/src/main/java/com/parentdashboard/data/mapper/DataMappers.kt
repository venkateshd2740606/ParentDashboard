package com.parentdashboard.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parentdashboard.data.local.database.entity.*
import com.parentdashboard.domain.model.*
import com.parentdashboard.engine.ParentDashboardGenerator

object DataMappers {
    private val gson = Gson()
    private val childListType = object : TypeToken<List<ChildProfile>>() {}.type
    private val progressMapType = object : TypeToken<Map<String, List<SubjectProgress>>>() {}.type
    private val logListType = object : TypeToken<List<ProgressLogEntry>>() {}.type
    private val stepListType = object : TypeToken<List<DashboardStepMode>>() {}.type

    fun toEntity(game: ParentDashboardGame): GameEntity {
        val state = GameStateJson(
            currentStepIndex = game.currentStepIndex,
            children = game.children,
            selectedChildId = game.selectedChildId,
            progressByChild = game.progressByChild,
            progressLog = game.progressLog,
            weeklyReportViewed = game.weeklyReportViewed,
            childrenAddedThisSession = game.childrenAddedThisSession,
            awaitingAdvance = game.awaitingAdvance
        )
        return GameEntity(
            id = game.id, seed = game.level.seed, levelNumber = game.level.levelNumber,
            difficulty = game.level.difficulty.name, status = game.status.name,
            tubeStateJson = gson.toJson(state), selectedTubeId = game.currentStepIndex,
            moves = game.moves, hintsUsed = game.hintsUsed, elapsedSeconds = game.elapsedSeconds,
            createdAt = game.createdAt, lastPlayedAt = game.lastPlayedAt, completedAt = game.completedAt,
            isTutorial = game.level.isTutorial, isEndless = game.level.isEndless,
            challengeType = game.level.challengeType?.name, challengeKey = game.level.challengeKey,
            levelJson = gson.toJson(toLevelJson(game.level)),
            coinsEarned = game.coinsEarned, xpEarned = game.xpEarned
        )
    }

    fun fromEntity(entity: GameEntity): ParentDashboardGame {
        val levelJson = gson.fromJson(entity.levelJson, LevelJson::class.java)
        val state = runCatching { gson.fromJson(entity.tubeStateJson, GameStateJson::class.java) }.getOrNull()
        val level = fromLevelJson(entity, levelJson)
        return if (state != null) {
            ParentDashboardGame(
                id = entity.id, level = level, status = GameStatus.valueOf(entity.status),
                currentStepIndex = state.currentStepIndex, children = state.children,
                selectedChildId = state.selectedChildId, progressByChild = state.progressByChild,
                progressLog = state.progressLog, weeklyReportViewed = state.weeklyReportViewed,
                childrenAddedThisSession = state.childrenAddedThisSession,
                awaitingAdvance = state.awaitingAdvance,
                hintsUsed = entity.hintsUsed, moves = entity.moves, elapsedSeconds = entity.elapsedSeconds,
                createdAt = entity.createdAt, lastPlayedAt = entity.lastPlayedAt,
                completedAt = entity.completedAt, coinsEarned = entity.coinsEarned, xpEarned = entity.xpEarned
            )
        } else {
            ParentDashboardGame(
                id = entity.id, level = level, status = GameStatus.valueOf(entity.status),
                currentStepIndex = entity.selectedTubeId.coerceAtLeast(0),
                hintsUsed = entity.hintsUsed, moves = entity.moves, elapsedSeconds = entity.elapsedSeconds,
                createdAt = entity.createdAt, lastPlayedAt = entity.lastPlayedAt,
                completedAt = entity.completedAt, coinsEarned = entity.coinsEarned, xpEarned = entity.xpEarned
            )
        }
    }

    private fun toLevelJson(level: ParentDashboardLevel) = LevelJson(
        title = level.title, instruction = level.instruction,
        taskType = level.taskType.name, steps = level.steps
    )

    private fun fromLevelJson(entity: GameEntity, json: LevelJson?): ParentDashboardLevel {
        if (json == null) return ParentDashboardGenerator.generate(entity.seed, entity.levelNumber, Difficulty.valueOf(entity.difficulty))
        return ParentDashboardLevel(
            id = entity.id, seed = entity.seed, levelNumber = entity.levelNumber,
            difficulty = Difficulty.valueOf(entity.difficulty),
            title = json.title, instruction = json.instruction,
            taskType = DashboardTaskType.valueOf(json.taskType), steps = json.steps,
            isTutorial = entity.isTutorial, isEndless = entity.isEndless,
            challengeType = entity.challengeType?.let { ChallengeType.valueOf(it) },
            challengeKey = entity.challengeKey
        )
    }

    fun toStatsEntity(stats: UserStats) = StatsEntity(
        gamesPlayed = stats.gamesPlayed, gamesWon = stats.gamesWon, gamesAbandoned = stats.gamesAbandoned,
        totalPlayTimeSeconds = stats.totalPlayTimeSeconds,
        fastestTimeBeginner = stats.fastestTimeBeginner, fastestTimeEasy = stats.fastestTimeEasy,
        fastestTimeMedium = stats.fastestTimeMedium, fastestTimeHard = stats.fastestTimeHard,
        fastestTimeExpert = stats.fastestTimeExpert, fastestTimeMaster = stats.fastestTimeMaster,
        currentStreak = stats.currentStreak, longestStreak = stats.longestStreak,
        lastPlayedDate = stats.lastPlayedDate, xpPoints = stats.xpPoints, level = stats.level,
        hintsUsedTotal = stats.hintsUsedTotal, perfectGames = stats.perfectGames,
        poursTotal = stats.poursTotal, endlessHighScore = stats.endlessHighScore
    )

    fun fromStatsEntity(entity: StatsEntity?) = entity?.let {
        UserStats(
            gamesPlayed = it.gamesPlayed, gamesWon = it.gamesWon, gamesAbandoned = it.gamesAbandoned,
            totalPlayTimeSeconds = it.totalPlayTimeSeconds,
            fastestTimeBeginner = it.fastestTimeBeginner, fastestTimeEasy = it.fastestTimeEasy,
            fastestTimeMedium = it.fastestTimeMedium, fastestTimeHard = it.fastestTimeHard,
            fastestTimeExpert = it.fastestTimeExpert, fastestTimeMaster = it.fastestTimeMaster,
            currentStreak = it.currentStreak, longestStreak = it.longestStreak,
            lastPlayedDate = it.lastPlayedDate, xpPoints = it.xpPoints, level = it.level,
            hintsUsedTotal = it.hintsUsedTotal, perfectGames = it.perfectGames,
            poursTotal = it.poursTotal, endlessHighScore = it.endlessHighScore
        )
    } ?: UserStats()

    fun toChallengeEntity(record: ChallengeRecord) = ChallengeEntity(
        key = record.key, type = record.type.name, seed = record.seed,
        difficulty = record.difficulty.name, isCompleted = record.isCompleted,
        completionTime = record.completionTime, moves = record.moves,
        rewardCoins = record.rewardCoins, rewardXp = record.rewardXp, streakDay = record.streakDay
    )

    fun fromChallengeEntity(entity: ChallengeEntity) = ChallengeRecord(
        key = entity.key, type = ChallengeType.valueOf(entity.type), seed = entity.seed,
        difficulty = Difficulty.valueOf(entity.difficulty), isCompleted = entity.isCompleted,
        completionTime = entity.completionTime, moves = entity.moves,
        rewardCoins = entity.rewardCoins, rewardXp = entity.rewardXp, streakDay = entity.streakDay
    )

    fun toEconomyEntity(state: EconomyState) = EconomyEntity(
        coins = state.coins, totalCoinsEarned = state.totalCoinsEarned,
        totalCoinsSpent = state.totalCoinsSpent,
        unlockedThemes = gson.toJson(state.unlockedThemeIds.toList())
    )

    fun fromEconomyEntity(entity: EconomyEntity?) = entity?.let {
        val type = object : TypeToken<List<String>>() {}.type
        val unlocked: List<String> = gson.fromJson(it.unlockedThemes, type) ?: emptyList()
        EconomyState(coins = it.coins, totalCoinsEarned = it.totalCoinsEarned,
            totalCoinsSpent = it.totalCoinsSpent, unlockedThemeIds = unlocked.toSet())
    } ?: EconomyState()

    fun mergeAchievement(def: Achievement, entity: AchievementEntity?) = def.copy(
        isUnlocked = entity?.isUnlocked ?: false, unlockedAt = entity?.unlockedAt,
        progress = entity?.progress ?: 0
    )

    fun toProfileEntity(profile: PuzzleProfile) = ProfileEntity(
        gamesAnalyzed = profile.metrics.gamesAnalyzed,
        totalSolveTimeSeconds = profile.metrics.totalSolveTimeSeconds,
        totalMoves = profile.metrics.totalMoves, totalOptimalMoves = profile.metrics.totalOptimalMoves,
        totalHintsUsed = profile.metrics.totalHintsUsed,
        fastCompletions = profile.metrics.fastCompletions, slowCompletions = profile.metrics.slowCompletions,
        perfectCompletions = profile.metrics.perfectCompletions,
        complexChainWins = profile.metrics.complexChainWins,
        inefficientWins = profile.metrics.inefficientWins, hintHeavyWins = profile.metrics.hintHeavyWins,
        archetype = profile.archetype.name, strength = profile.strength.name,
        weakness = profile.weakness.name, adaptiveColorModifier = profile.adaptiveColorModifier
    )

    fun fromProfileEntity(entity: ProfileEntity?) = entity?.let {
        PuzzleProfile(
            metrics = PuzzleProfileMetrics(
                gamesAnalyzed = it.gamesAnalyzed, totalSolveTimeSeconds = it.totalSolveTimeSeconds,
                totalMoves = it.totalMoves, totalOptimalMoves = it.totalOptimalMoves,
                totalHintsUsed = it.totalHintsUsed, fastCompletions = it.fastCompletions,
                slowCompletions = it.slowCompletions, perfectCompletions = it.perfectCompletions,
                complexChainWins = it.complexChainWins, inefficientWins = it.inefficientWins,
                hintHeavyWins = it.hintHeavyWins
            ),
            archetype = runCatching { PuzzleArchetype.valueOf(it.archetype) }.getOrDefault(PuzzleArchetype.EXPLORER),
            strength = runCatching { SkillCategory.valueOf(it.strength) }.getOrDefault(SkillCategory.PLANNING),
            weakness = runCatching { SkillCategory.valueOf(it.weakness) }.getOrDefault(SkillCategory.TIME_PRESSURE),
            adaptiveColorModifier = it.adaptiveColorModifier
        )
    } ?: PuzzleProfile()

    data class LevelJson(val title: String, val instruction: String, val taskType: String, val steps: List<DashboardStepMode>)
    data class GameStateJson(
        val currentStepIndex: Int = 0, val children: List<ChildProfile> = emptyList(),
        val selectedChildId: String? = null,
        val progressByChild: Map<String, List<SubjectProgress>> = emptyMap(),
        val progressLog: List<ProgressLogEntry> = emptyList(),
        val weeklyReportViewed: Boolean = false, val childrenAddedThisSession: Int = 0,
        val awaitingAdvance: Boolean = false
    )
}
