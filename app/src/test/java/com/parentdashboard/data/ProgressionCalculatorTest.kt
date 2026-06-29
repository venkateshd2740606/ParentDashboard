package com.parentdashboard.data

import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.GameStatus
import com.parentdashboard.engine.ParentDashboardEngine
import com.parentdashboard.engine.ParentDashboardGenerator
import com.parentdashboard.util.ProgressionCalculator
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionCalculatorTest {

    @Test
    fun xpForCompletedGame_isPositive() {
        val level = ParentDashboardGenerator.generate(1L, 1, Difficulty.EASY)
        val game = ParentDashboardEngine.createInitialGame(level).copy(status = GameStatus.COMPLETED)
        assertTrue(ProgressionCalculator.xpForGame(game) > 0)
    }

    @Test
    fun xpForGame_withHints_isLowerThanWithoutHints() {
        val level = ParentDashboardGenerator.generate(1L, 1, Difficulty.EASY)
        val withHints = ParentDashboardEngine.createInitialGame(level).copy(hintsUsed = 2, status = GameStatus.COMPLETED)
        val noHints = ParentDashboardEngine.createInitialGame(level).copy(hintsUsed = 0, status = GameStatus.COMPLETED)
        assertTrue(ProgressionCalculator.xpForGame(noHints) >= ProgressionCalculator.xpForGame(withHints))
    }
}
