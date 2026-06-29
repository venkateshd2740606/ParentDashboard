package com.parentdashboard.engine

import com.parentdashboard.domain.model.*

object ParentDashboardGenerator {

    private val campaignTasks = listOf(
        Triple(DashboardTaskType.WELCOME, "Welcome", "Explore the parent dashboard overview."),
        Triple(DashboardTaskType.ADD_CHILD, "Add Child", "Tap Add Child and create a new profile."),
        Triple(DashboardTaskType.SELECT_CHILD, "Select Child", "Choose a child from the switcher."),
        Triple(DashboardTaskType.LOG_FIRST_ACTIVITY, "Log Activity", "Record progress for any subject."),
        Triple(DashboardTaskType.VIEW_WEEKLY_REPORT, "Weekly Report", "Open and review the 7-day summary."),
        Triple(DashboardTaskType.VIEW_SUBJECT_PROGRESS, "Subject Cards", "Review all subject progress cards."),
        Triple(DashboardTaskType.LOG_MULTI_SUBJECT, "Multi-Subject", "Log progress for two different subjects."),
        Triple(DashboardTaskType.ADD_SECOND_CHILD, "Second Child", "Add another child profile."),
        Triple(DashboardTaskType.SWITCH_CHILD, "Switch Child", "Switch between child profiles."),
        Triple(DashboardTaskType.REVIEW_STARS, "Star Review", "Check star totals on subject cards."),
        Triple(DashboardTaskType.LOG_ENGLISH, "English Progress", "Log English learning progress."),
        Triple(DashboardTaskType.LOG_RHYMES, "Rhymes Progress", "Log rhymes learning progress."),
        Triple(DashboardTaskType.COMPARE_CHILDREN, "Compare", "Compare progress across children."),
        Triple(DashboardTaskType.EXPORT_SUMMARY, "Export Summary", "View the weekly export summary."),
        Triple(DashboardTaskType.CELEBRATE, "Celebrate!", "Complete your dashboard setup journey.")
    )

    fun generate(seed: Long, levelNumber: Int, difficulty: Difficulty): ParentDashboardLevel {
        val index = (levelNumber - 1).coerceIn(0, campaignTasks.lastIndex)
        val (task, title, instruction) = campaignTasks[index]
        return buildLevel(seed, levelNumber, difficulty, task, title, instruction,
            isTutorial = levelNumber <= ParentDashboardLevel.TUTORIAL_COUNT)
    }

    fun generateForChallenge(seed: Long, levelNumber: Int, difficulty: Difficulty): ParentDashboardLevel =
        generate(seed, levelNumber, difficulty)

    fun seedFromLevelNumber(levelNumber: Int, difficulty: Difficulty): Long =
        levelNumber.toLong() * 9973L + difficulty.ordinal * 100_000L + 99L

    fun formatShareText(seed: Long, levelNumber: Int, difficulty: Difficulty): String =
        "Parent Dashboard Task\nSeed: $seed\nLevel: $levelNumber\nDifficulty: ${difficulty.name}"

    fun buildLevel(
        seed: Long, levelNumber: Int, difficulty: Difficulty,
        taskType: DashboardTaskType, title: String, instruction: String,
        isTutorial: Boolean = false, isEndless: Boolean = false
    ): ParentDashboardLevel = ParentDashboardLevel(
        seed = seed, levelNumber = levelNumber, difficulty = difficulty,
        title = title, instruction = instruction, taskType = taskType,
        isTutorial = isTutorial, isEndless = isEndless
    )
}
