package com.parentdashboard.engine

import com.parentdashboard.domain.model.DashboardTaskType
import com.parentdashboard.domain.model.Difficulty

object TutorialLevels {
    val all = listOf(
        build(1, DashboardTaskType.WELCOME, "Welcome", "Explore the parent dashboard overview."),
        build(2, DashboardTaskType.ADD_CHILD, "Add Child", "Tap Add Child and create a new profile."),
        build(3, DashboardTaskType.SELECT_CHILD, "Select Child", "Choose a child from the switcher."),
        build(4, DashboardTaskType.LOG_FIRST_ACTIVITY, "Log Activity", "Record progress for any subject."),
        build(5, DashboardTaskType.VIEW_WEEKLY_REPORT, "Weekly Report", "Open and review the 7-day summary.")
    )

    private fun build(n: Int, task: DashboardTaskType, title: String, instruction: String) =
        ParentDashboardGenerator.buildLevel(n.toLong(), n, Difficulty.BEGINNER, task, title, instruction, isTutorial = true)

    fun getTutorialLevel(index: Int) = all.getOrNull(index)
}
