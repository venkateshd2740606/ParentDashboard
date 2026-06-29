package com.parentdashboard.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.parentdashboard.domain.model.Difficulty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor() {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    fun logGameStarted(difficulty: Difficulty, levelNumber: Int) {
        analytics.logEvent("game_started", Bundle().apply {
            putString("difficulty", difficulty.name)
            putInt("level_number", levelNumber)
        })
    }

    fun logGameCompleted(difficulty: Difficulty, timeSeconds: Long, moves: Int, hintsUsed: Int) {
        analytics.logEvent(FirebaseAnalytics.Event.LEVEL_END, Bundle().apply {
            putString("difficulty", difficulty.name)
            putLong("time_seconds", timeSeconds)
            putInt("moves", moves)
            putInt("hints_used", hintsUsed)
        })
    }

    fun logChallengeCompleted(type: String, streak: Int) {
        analytics.logEvent("challenge_completed", Bundle().apply {
            putString("challenge_type", type)
            putInt("streak", streak)
        })
    }

    fun logAchievementUnlocked(id: String) {
        analytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id)
        })
    }

    fun logAdWatched(adType: String) {
        analytics.logEvent("ad_watched", Bundle().apply { putString("ad_type", adType) })
    }

    fun logMultiplayerStarted(mode: String) {
        analytics.logEvent("multiplayer_started", Bundle().apply { putString("mode", mode) })
    }

    fun setUserLevel(level: Int) = analytics.setUserProperty("player_level", level.toString())
}
