package com.parentdashboard.engine

import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.LearningLanguage
import com.parentdashboard.domain.model.LearningSubject
import org.junit.Assert.*
import org.junit.Test

class ParentDashboardEngineTest {

    @Test fun demoData_hasTwoChildren() {
        assertEquals(2, ParentDashboardEngine.demoChildren().size)
    }

    @Test fun addChild_increasesCount() {
        val level = TutorialLevels.getTutorialLevel(1)!!
        var game = ParentDashboardEngine.createInitialGame(level)
        val before = game.children.size
        game = ParentDashboardEngine.addChild(game, "Meera", 6, "👧")
        assertEquals(before + 1, game.children.size)
        assertEquals(1, game.childrenAddedThisSession)
    }

    @Test fun selectChild_setsSelectedId() {
        val level = TutorialLevels.getTutorialLevel(2)!!
        var game = ParentDashboardEngine.createInitialGame(level)
        game = ParentDashboardEngine.selectChild(game, game.children.first().id)
        assertNotNull(game.selectedChildId)
    }

    @Test fun logProgress_addsEntry() {
        val level = TutorialLevels.getTutorialLevel(3)!!
        var game = ParentDashboardEngine.createInitialGame(level)
        game = ParentDashboardEngine.selectChild(game, game.children.first().id)
        val before = game.progressLog.size
        game = ParentDashboardEngine.logProgress(game, LearningSubject.ABC, 50, 2)
        assertTrue(game.progressLog.size > before)
    }

    @Test fun weeklyReport_returnsAllSubjects() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        val game = ParentDashboardEngine.createInitialGame(level)
        val report = ParentDashboardEngine.weeklyReport(game, game.children.first().id)
        assertEquals(LearningSubject.entries.size, report.size)
    }

    @Test fun weeklyReport_includesLanguagesPracticed() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        val game = ParentDashboardEngine.createInitialGame(level)
        val report = ParentDashboardEngine.weeklyReport(game, game.children.first().id)
        val languages = ParentDashboardEngine.languagesPracticedThisWeek(game, game.children.first().id)
        assertTrue(report.any { it.languagesPracticed.isNotEmpty() })
        assertTrue(languages.contains(LearningLanguage.HINDI))
    }

    @Test fun addChild_usesPreferredLanguage() {
        val level = TutorialLevels.getTutorialLevel(1)!!
        var game = ParentDashboardEngine.createInitialGame(level)
        game = ParentDashboardEngine.addChild(game, "Meera", 6, "👧", LearningLanguage.TAMIL)
        val added = game.children.last()
        assertEquals(LearningLanguage.TAMIL, added.preferredLanguage)
    }

    @Test fun demoChildren_havePreferredLanguages() {
        val children = ParentDashboardEngine.demoChildren()
        assertEquals(LearningLanguage.HINDI, children.first().preferredLanguage)
        assertEquals(LearningLanguage.TELUGU, children.last().preferredLanguage)
    }

    @Test fun tutorial_completeFlow() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        var game = ParentDashboardEngine.createInitialGame(level)
        game = ParentDashboardEngine.nextStep(game)
        game = ParentDashboardEngine.nextStep(game)
        game = ParentDashboardEngine.nextStep(game)
        assertTrue(game.isCompleted)
    }
}
