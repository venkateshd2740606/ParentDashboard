package com.parentdashboard.util

import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.ParentDashboardGame
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ProgressionCalculator {
    fun xpForGame(game: ParentDashboardGame): Int {
        val base = when (game.level.difficulty) {
            Difficulty.BEGINNER -> 10
            Difficulty.EASY -> 20
            Difficulty.MEDIUM -> 35
            Difficulty.HARD -> 50
            Difficulty.EXPERT -> 75
            Difficulty.MASTER -> 100
            Difficulty.ENDLESS -> 30 + game.level.levelNumber * 5
        }
        val moveBonus = maxOf(0, 20 - game.moves)
        val hintPenalty = game.hintsUsed * 5
        val timeBonus = if (game.elapsedSeconds < 120) 15 else if (game.elapsedSeconds < 300) 5 else 0
        return maxOf(5, ((base + moveBonus + timeBonus - hintPenalty) * game.level.difficulty.xpMultiplier).toInt())
    }

    fun coinsForGame(game: ParentDashboardGame): Int {
        val base = when (game.level.difficulty) {
            Difficulty.BEGINNER -> 5
            Difficulty.EASY -> 10
            Difficulty.MEDIUM -> 15
            Difficulty.HARD -> 25
            Difficulty.EXPERT -> 40
            Difficulty.MASTER -> 60
            Difficulty.ENDLESS -> 10 + game.level.levelNumber * 2
        }
        return base + if (game.hintsUsed == 0) 5 else 0
    }

    fun levelFromXp(xp: Long): Int {
        var level = 1
        var required = 100L
        var remaining = xp
        while (remaining >= required && level < 200) {
            remaining -= required
            level++
            required = (required * 1.15).toLong()
        }
        return level
    }

    fun xpToNextLevel(level: Int, xp: Long): Pair<Long, Long> {
        var totalForLevel = 0L
        var required = 100L
        repeat(level - 1) {
            totalForLevel += required
            required = (required * 1.15).toLong()
        }
        val currentLevelXp = xp - totalForLevel
        return currentLevelXp to required
    }
}

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val weekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun todayKey(): String = dateFormat.format(Calendar.getInstance().time)
    fun weekKey(): String = weekFormat.format(Calendar.getInstance().time)
    fun monthKey(): String = monthFormat.format(Calendar.getInstance().time)

    fun isConsecutiveDay(previous: String, current: String): Boolean {
        if (previous.isBlank()) return false
        val prev = dateFormat.parse(previous) ?: return false
        val curr = dateFormat.parse(current) ?: return false
        val cal = Calendar.getInstance()
        cal.time = prev
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(cal.time) == current
    }

    fun calculateStreak(completedKeys: List<String>): Int {
        if (completedKeys.isEmpty()) return 0
        val sorted = completedKeys.sortedDescending()
        var streak = 1
        for (i in 0 until sorted.size - 1) {
            if (isConsecutiveDay(sorted[i + 1], sorted[i])) streak++ else break
        }
        return streak
    }
}
