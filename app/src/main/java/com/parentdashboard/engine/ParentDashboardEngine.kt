package com.parentdashboard.engine

import com.parentdashboard.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ParentDashboardEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000

    fun demoChildren(): List<ChildProfile> = listOf(
        ChildProfile("demo-aanya", "Aanya", 5, "👧", LearningLanguage.HINDI),
        ChildProfile("demo-ravi", "Ravi", 7, "👦", LearningLanguage.TELUGU)
    )

    fun demoProgress(): Map<String, List<SubjectProgress>> {
        val today = dateFormat.format(Date())
        return mapOf(
            "demo-aanya" to listOf(
                SubjectProgress(LearningSubject.ABC, 40, 8, today),
                SubjectProgress(LearningSubject.NUM123, 25, 5, today),
                SubjectProgress(LearningSubject.MISSING_MATCH, 15, 3, today)
            ),
            "demo-ravi" to listOf(
                SubjectProgress(LearningSubject.MATH, 55, 11, today),
                SubjectProgress(LearningSubject.ENGLISH, 30, 6, today),
                SubjectProgress(LearningSubject.MISSING_MATCH, 20, 4, today)
            )
        )
    }

    fun demoLog(): List<ProgressLogEntry> {
        val now = System.currentTimeMillis()
        return listOf(
            ProgressLogEntry("demo-aanya", LearningSubject.ABC, 40, 2, now - 86400000, LearningLanguage.HINDI),
            ProgressLogEntry("demo-ravi", LearningSubject.MATH, 55, 3, now - 43200000, LearningLanguage.TELUGU),
            ProgressLogEntry("demo-aanya", LearningSubject.MISSING_MATCH, 15, 1, now - 3600000, LearningLanguage.HINDI)
        )
    }

    fun createInitialGame(level: ParentDashboardLevel): ParentDashboardGame =
        ParentDashboardGame(
            level = level,
            children = demoChildren(),
            progressByChild = demoProgress(),
            progressLog = demoLog()
        )

    fun validateLevel(level: ParentDashboardLevel): Boolean =
        level.title.isNotBlank() && level.instruction.isNotBlank()

    fun currentStep(game: ParentDashboardGame): DashboardStepMode? = game.currentStep

    fun canNextStep(game: ParentDashboardGame): Boolean {
        if (game.isCompleted || game.awaitingAdvance) return false
        return when (game.currentStep) {
            DashboardStepMode.INTRO -> true
            DashboardStepMode.ACTION -> isTaskComplete(game)
            DashboardStepMode.REVIEW -> true
            null -> false
        }
    }

    fun nextStep(game: ParentDashboardGame): ParentDashboardGame {
        if (!canNextStep(game)) return game
        val now = System.currentTimeMillis()
        val nextIndex = game.currentStepIndex + 1
        if (nextIndex >= game.level.steps.size) {
            return game.copy(
                status = GameStatus.COMPLETED,
                completedAt = now,
                lastPlayedAt = now,
                moves = game.moves + 1
            )
        }
        return game.copy(
            currentStepIndex = nextIndex,
            awaitingAdvance = false,
            moves = game.moves + 1,
            lastPlayedAt = now
        )
    }

    fun addChild(
        game: ParentDashboardGame,
        name: String,
        age: Int,
        avatarEmoji: String,
        defaultLanguage: LearningLanguage = LearningLanguage.ENGLISH
    ): ParentDashboardGame {
        if (name.isBlank()) return game
        val child = ChildProfile(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            age = age.coerceIn(2, 12),
            avatarEmoji = avatarEmoji,
            preferredLanguage = defaultLanguage
        )
        val updatedChildren = game.children + child
        val defaultProgress = LearningSubject.entries.map { subject ->
            SubjectProgress(subject, 0, 0, dateFormat.format(Date()))
        }
        return game.copy(
            children = updatedChildren,
            progressByChild = game.progressByChild + (child.id to defaultProgress),
            childrenAddedThisSession = game.childrenAddedThisSession + 1,
            moves = game.moves + 1,
            lastPlayedAt = System.currentTimeMillis()
        )
    }

    fun selectChild(game: ParentDashboardGame, childId: String): ParentDashboardGame {
        if (game.children.none { it.id == childId }) return game
        return game.copy(
            selectedChildId = childId,
            moves = game.moves + 1,
            lastPlayedAt = System.currentTimeMillis()
        )
    }

    fun logProgress(
        game: ParentDashboardGame,
        subject: LearningSubject,
        percent: Int,
        stars: Int
    ): ParentDashboardGame {
        val childId = game.selectedChildId ?: return game
        val clampedPercent = percent.coerceIn(0, 100)
        val clampedStars = stars.coerceIn(0, 5)
        val today = dateFormat.format(Date())
        val now = System.currentTimeMillis()
        val existing = game.progressByChild[childId].orEmpty()
        val updated = existing.map { progress ->
            if (progress.subject == subject) {
                progress.copy(
                    percentComplete = maxOf(progress.percentComplete, clampedPercent),
                    stars = progress.stars + clampedStars,
                    lastPlayedDate = today
                )
            } else progress
        }.ifEmpty {
            LearningSubject.entries.map { s ->
                SubjectProgress(s, if (s == subject) clampedPercent else 0,
                    if (s == subject) clampedStars else 0, today)
            }
        }
        val entry = ProgressLogEntry(
            childId = childId,
            subject = subject,
            percent = clampedPercent,
            stars = clampedStars,
            timestamp = now,
            learningLanguage = game.selectedChild?.preferredLanguage
        )
        return game.copy(
            progressByChild = game.progressByChild + (childId to updated),
            progressLog = game.progressLog + entry,
            moves = game.moves + 1,
            lastPlayedAt = now
        )
    }

    fun markWeeklyReportViewed(game: ParentDashboardGame): ParentDashboardGame =
        game.copy(weeklyReportViewed = true, moves = game.moves + 1, lastPlayedAt = System.currentTimeMillis())

    fun weeklyReport(game: ParentDashboardGame, childId: String? = game.selectedChildId): List<WeeklyReportEntry> {
        val targetId = childId ?: return emptyList()
        val cutoff = System.currentTimeMillis() - WEEK_MS
        val recent = game.progressLog.filter { it.childId == targetId && it.timestamp >= cutoff }
        return LearningSubject.entries.map { subject ->
            val entries = recent.filter { it.subject == subject }
            WeeklyReportEntry(
                subject = subject,
                totalStars = entries.sumOf { it.stars },
                averagePercent = if (entries.isEmpty()) 0 else entries.map { it.percent }.average().toInt(),
                sessionCount = entries.size,
                languagesPracticed = entries.mapNotNull { it.learningLanguage }.toSet()
            )
        }
    }

    fun languagesPracticedThisWeek(
        game: ParentDashboardGame,
        childId: String? = game.selectedChildId
    ): Set<LearningLanguage> =
        weeklyReport(game, childId).flatMap { it.languagesPracticed }.toSet()

    fun isTaskComplete(game: ParentDashboardGame): Boolean = when (game.level.taskType) {
        DashboardTaskType.WELCOME -> true
        DashboardTaskType.ADD_CHILD -> game.childrenAddedThisSession >= 1
        DashboardTaskType.SELECT_CHILD -> game.selectedChildId != null
        DashboardTaskType.LOG_FIRST_ACTIVITY -> sessionLogCount(game) >= 1
        DashboardTaskType.VIEW_SUBJECT_PROGRESS -> game.selectedChildId != null
        DashboardTaskType.VIEW_WEEKLY_REPORT -> game.weeklyReportViewed
        DashboardTaskType.ADD_SECOND_CHILD -> game.children.size >= 3
        DashboardTaskType.SWITCH_CHILD -> game.moves >= 2 && game.selectedChildId != null
        DashboardTaskType.LOG_MULTI_SUBJECT -> distinctSubjectsLoggedThisSession(game) >= 2
        DashboardTaskType.REVIEW_STARS -> game.selectedChildId != null
        DashboardTaskType.LOG_ENGLISH -> hasLoggedSubject(game, LearningSubject.ENGLISH)
        DashboardTaskType.LOG_RHYMES -> hasLoggedSubject(game, LearningSubject.RHYMES)
        DashboardTaskType.COMPARE_CHILDREN -> game.children.size >= 2
        DashboardTaskType.EXPORT_SUMMARY -> game.weeklyReportViewed
        DashboardTaskType.CELEBRATE -> sessionLogCount(game) >= 1
    }

    private fun sessionLogCount(game: ParentDashboardGame): Int =
        game.progressLog.count { it.timestamp >= game.createdAt }

    private fun distinctSubjectsLoggedThisSession(game: ParentDashboardGame): Int =
        game.progressLog.filter { it.timestamp >= game.createdAt }.map { it.subject }.distinct().size

    private fun hasLoggedSubject(game: ParentDashboardGame, subject: LearningSubject): Boolean =
        game.progressLog.any { it.timestamp >= game.createdAt && it.subject == subject }

    fun isWon(game: ParentDashboardGame): Boolean = game.isCompleted
    fun optimalScore(game: ParentDashboardGame): Int = 1

    fun canUseHint(game: ParentDashboardGame): Boolean = false
    fun useHint(game: ParentDashboardGame): ParentDashboardGame = game

    fun formatP2PMove(action: String, payload: String = ""): String =
        if (payload.isEmpty()) action else "$action:$payload"

    fun applyRemoteMove(game: ParentDashboardGame, payload: String): ParentDashboardGame {
        val parts = payload.split(":", limit = 2)
        return when (parts.firstOrNull()) {
            "next" -> nextStep(game)
            "select" -> parts.getOrNull(1)?.let { selectChild(game, it) } ?: game
            else -> game
        }
    }

    fun botConfirmAction(game: ParentDashboardGame, @Suppress("UNUSED_PARAMETER") accuracy: Float = 0.7f): Boolean =
        isTaskComplete(game)
}
