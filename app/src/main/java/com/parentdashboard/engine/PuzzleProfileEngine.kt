package com.parentdashboard.engine

import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.GameStatus
import com.parentdashboard.domain.model.GenerationProfile
import com.parentdashboard.domain.model.PuzzleArchetype
import com.parentdashboard.domain.model.PuzzleProfile
import com.parentdashboard.domain.model.PuzzleProfileMetrics
import com.parentdashboard.domain.model.ParentDashboardGame
import com.parentdashboard.domain.model.SkillCategory
import kotlin.math.max
import kotlin.math.roundToInt

object PuzzleProfileEngine {

    private const val SECONDS_PER_STEP_FAST = 10
    private const val SECONDS_PER_STEP_SLOW = 35

    fun updateMetrics(current: PuzzleProfileMetrics, game: ParentDashboardGame): PuzzleProfileMetrics {
        if (game.status != GameStatus.COMPLETED || game.level.isTutorial) return current

        val stepCount = max(game.level.stepCount, 1)
        val optimalScore = ParentDashboardEngine.optimalScore(game)
        val secondsPerStep = game.elapsedSeconds.toFloat() / stepCount
        val isFast = secondsPerStep <= SECONDS_PER_STEP_FAST
        val isSlow = secondsPerStep >= SECONDS_PER_STEP_SLOW
        val isPerfect = game.hintsUsed == 0
        val isComplex = game.level.levelNumber >= 10
        val isInefficient = game.hintsUsed > 0
        val isHintHeavy = game.hintsUsed >= 1

        return current.copy(
            gamesAnalyzed = current.gamesAnalyzed + 1,
            totalSolveTimeSeconds = current.totalSolveTimeSeconds + game.elapsedSeconds,
            totalMoves = current.totalMoves + game.moves,
            totalOptimalMoves = current.totalOptimalMoves + stepCount,
            totalHintsUsed = current.totalHintsUsed + game.hintsUsed,
            fastCompletions = current.fastCompletions + if (isFast) 1 else 0,
            slowCompletions = current.slowCompletions + if (isSlow) 1 else 0,
            perfectCompletions = current.perfectCompletions + if (isPerfect) 1 else 0,
            complexChainWins = current.complexChainWins + if (isComplex) 1 else 0,
            inefficientWins = current.inefficientWins + if (isInefficient) 1 else 0,
            hintHeavyWins = current.hintHeavyWins + if (isHintHeavy) 1 else 0
        )
    }

    fun buildProfile(metrics: PuzzleProfileMetrics): PuzzleProfile {
        if (metrics.gamesAnalyzed == 0) {
            return PuzzleProfile(
                metrics = metrics,
                archetype = PuzzleArchetype.EXPLORER,
                strength = SkillCategory.PATTERN_RECOGNITION,
                weakness = SkillCategory.TIME_PRESSURE,
                adaptiveColorModifier = 0
            )
        }

        val scores = categoryScores(metrics)
        val strength = scores.maxBy { it.value }.key
        val weakness = scores.minBy { it.value }.key
        val archetype = resolveArchetype(metrics)
        val adaptiveColorModifier = resolveAdaptiveModifier(metrics, scores)

        return PuzzleProfile(
            metrics = metrics,
            archetype = archetype,
            strength = strength,
            weakness = weakness,
            adaptiveColorModifier = adaptiveColorModifier
        )
    }

    fun adaptiveGenerationProfile(profile: PuzzleProfile): GenerationProfile {
        val modifier = profile.adaptiveColorModifier.coerceIn(-1, 2)
        return GenerationProfile(taskOffsetModifier = modifier)
    }

    fun percentileTopValue(profile: PuzzleProfile, category: SkillCategory): Int {
        val score = categoryScores(profile.metrics)[category] ?: 50
        return (100 - score.coerceIn(5, 98))
    }

    fun percentileLabel(profile: PuzzleProfile, category: SkillCategory): String {
        val score = categoryScores(profile.metrics)[category] ?: 50
        val percentile = score.coerceIn(5, 98)
        return "Top ${100 - percentile}%"
    }

    private fun resolveArchetype(metrics: PuzzleProfileMetrics): PuzzleArchetype {
        val games = metrics.gamesAnalyzed.toFloat()
        val hintRate = metrics.totalHintsUsed / games
        val fastRate = metrics.fastCompletions / games
        val slowRate = metrics.slowCompletions / games
        val perfectRate = metrics.perfectCompletions / games
        val inefficientRate = metrics.inefficientWins / games
        val complexRate = metrics.complexChainWins / games
        val stepEfficiency = if (metrics.totalMoves > 0) {
            metrics.totalOptimalMoves.toFloat() / metrics.totalMoves
        } else 1f

        return when {
            perfectRate >= 0.35f && stepEfficiency >= 0.85f -> PuzzleArchetype.ARCHITECT
            fastRate >= 0.4f && hintRate <= 0.8f -> PuzzleArchetype.SPRINTER
            hintRate >= 1.0f && slowRate >= 0.25f -> PuzzleArchetype.ANALYST
            complexRate >= 0.35f -> PuzzleArchetype.STRATEGIST
            inefficientRate >= 0.3f -> PuzzleArchetype.EXPLORER
            stepEfficiency >= 0.8f -> PuzzleArchetype.ARCHITECT
            else -> PuzzleArchetype.EXPLORER
        }
    }

    private fun categoryScores(metrics: PuzzleProfileMetrics): Map<SkillCategory, Int> {
        if (metrics.gamesAnalyzed == 0) {
            return SkillCategory.entries.associateWith { 50 }
        }
        val games = metrics.gamesAnalyzed.toFloat()
        val stepEfficiency = if (metrics.totalMoves > 0) {
            metrics.totalOptimalMoves.toFloat() / metrics.totalMoves
        } else 0.5f
        val avgHints = metrics.totalHintsUsed / games
        val avgSecondsPerStep = if (metrics.totalMoves > 0) {
            metrics.totalSolveTimeSeconds.toFloat() / metrics.totalMoves
        } else 10f

        return mapOf(
            SkillCategory.PATTERN_RECOGNITION to score(
                stepEfficiency * 100f + metrics.perfectCompletions / games * 20f
            ),
            SkillCategory.PLANNING to score(
                stepEfficiency * 90f + metrics.perfectCompletions / games * 30f
            ),
            SkillCategory.SPEED to score(
                metrics.fastCompletions / games * 100f - metrics.slowCompletions / games * 20f + 40f
            ),
            SkillCategory.ACCURACY to score(
                (1f - avgHints / 3f) * 70f + metrics.perfectCompletions / games * 40f
            ),
            SkillCategory.COMPLEX_CHAINS to score(
                metrics.complexChainWins / games * 100f + stepEfficiency * 20f
            ),
            SkillCategory.TIME_PRESSURE to score(
                100f - metrics.slowCompletions / games * 50f - avgSecondsPerStep * 2f + 30f
            )
        )
    }

    private fun resolveAdaptiveModifier(
        metrics: PuzzleProfileMetrics,
        scores: Map<SkillCategory, Int>
    ): Int {
        val weaknessScore = scores.values.minOrNull() ?: 50
        val strengthScore = scores.values.maxOrNull() ?: 50
        return when {
            weaknessScore < 35 && metrics.slowCompletions > metrics.fastCompletions -> -1
            strengthScore > 75 && metrics.perfectCompletions >= 3 -> 1
            strengthScore > 85 && metrics.gamesAnalyzed >= 10 -> 2
            else -> 0
        }
    }

    private fun score(raw: Float): Int = raw.roundToInt().coerceIn(0, 99)
}

