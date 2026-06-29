package com.parentdashboard.domain.model

enum class Difficulty(val stars: Int, val xpMultiplier: Float) {
    BEGINNER(1, 0.5f), EASY(2, 1.0f), MEDIUM(3, 1.5f), HARD(4, 2.0f),
    EXPERT(5, 2.5f), MASTER(6, 3.0f), ENDLESS(0, 1.0f);
    val displayName: String get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class GameStatus { IN_PROGRESS, COMPLETED, FAILED, PAUSED }

enum class AppTheme(val displayName: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark"), AMOLED("AMOLED"),
    NEON("Neon"), CYBER("Cyber"), SPACE("Space"), NATURE("Nature")
}

enum class ColorBlindMode { NONE, DEUTERANOPIA, PROTANOPIA, TRITANOPIA }
enum class ChallengeType { DAILY, WEEKLY, MONTHLY }

enum class PlayerRank(val minLevel: Int, val title: String) {
    NOVICE(1, "Novice"), APPRENTICE(5, "Apprentice"), PUZZLER(10, "Puzzler"),
    STRATEGIST(20, "Strategist"), EXPERT(35, "Expert"), MASTER(50, "Master"),
    GRANDMASTER(75, "Grandmaster"), LEGEND(100, "Legend");
    companion object {
        fun fromLevel(level: Int): PlayerRank =
            entries.filter { level >= it.minLevel }.maxByOrNull { it.minLevel } ?: NOVICE
    }
}

enum class MultiplayerMode { SAME_DEVICE, LOCAL_P2P }
enum class P2PConnectionType(val displayName: String) {
    NEARBY("Nearby (Bluetooth + Wi-Fi)"), WIFI_LAN("Same Wi-Fi"), WIFI_DIRECT("Wi-Fi Direct")
}
enum class P2PRole { HOST, CLIENT }
enum class P2PSessionState { IDLE, ADVERTISING, DISCOVERING, CONNECTING, CONNECTED, ERROR }

enum class LearningLanguage(val childLabel: String, val parentLabel: String, val badge: String) {
    ENGLISH("English", "English", "EN"),
    HINDI("हिंदी", "Hindi", "HI"),
    TELUGU("తెలుగు", "Telugu", "TE"),
    TAMIL("தமிழ்", "Tamil", "TA"),
    KANNADA("ಕನ್ನಡ", "Kannada", "KN"),
    MALAYALAM("മലയാളം", "Malayalam", "ML")
}

enum class LearningSubject(val label: String, val parentDescription: String = "") {
    ABC("ABC", "Letter recognition"),
    NUM123("123", "Number skills"),
    MATH("Math", "Addition and subtraction"),
    ENGLISH("English", "Reading and vocabulary"),
    RHYMES("Rhymes", "Songs and rhythm"),
    MISSING_MATCH("Missing & Match", "Fill-in puzzles — also in KidsMatch app")
}

enum class DashboardStepMode { INTRO, ACTION, REVIEW }

enum class DashboardTaskType {
    WELCOME, ADD_CHILD, SELECT_CHILD, LOG_FIRST_ACTIVITY, VIEW_SUBJECT_PROGRESS,
    VIEW_WEEKLY_REPORT, ADD_SECOND_CHILD, SWITCH_CHILD, LOG_MULTI_SUBJECT,
    REVIEW_STARS, LOG_ENGLISH, LOG_RHYMES, COMPARE_CHILDREN, EXPORT_SUMMARY, CELEBRATE
}

enum class PuzzleArchetype(val title: String, val description: String) {
    ARCHITECT("The Architect", "You plan ahead."),
    SPRINTER("The Sprinter", "You move quickly."),
    ANALYST("The Analyst", "You review details carefully."),
    EXPLORER("The Explorer", "You explore all features."),
    STRATEGIST("The Strategist", "You track progress systematically.")
}

enum class SkillCategory(val label: String) {
    PATTERN_RECOGNITION("Pattern Recognition"), PLANNING("Planning"), SPEED("Speed"),
    ACCURACY("Accuracy"), COMPLEX_CHAINS("Complex Chains"), TIME_PRESSURE("Time Pressure")
}

data class PuzzleProfileMetrics(
    val gamesAnalyzed: Int = 0, val totalSolveTimeSeconds: Long = 0,
    val totalMoves: Int = 0, val totalOptimalMoves: Int = 0, val totalHintsUsed: Int = 0,
    val fastCompletions: Int = 0, val slowCompletions: Int = 0, val perfectCompletions: Int = 0,
    val complexChainWins: Int = 0, val inefficientWins: Int = 0, val hintHeavyWins: Int = 0
)

data class PuzzleProfile(
    val metrics: PuzzleProfileMetrics = PuzzleProfileMetrics(),
    val archetype: PuzzleArchetype = PuzzleArchetype.EXPLORER,
    val strength: SkillCategory = SkillCategory.PLANNING,
    val weakness: SkillCategory = SkillCategory.TIME_PRESSURE,
    val adaptiveColorModifier: Int = 0
)

data class GenerationProfile(val taskOffsetModifier: Int = 0)

data class ChildProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatarEmoji: String,
    val preferredLanguage: LearningLanguage = LearningLanguage.ENGLISH
)

data class SubjectProgress(
    val subject: LearningSubject,
    val percentComplete: Int,
    val stars: Int,
    val lastPlayedDate: String
)

data class ProgressLogEntry(
    val childId: String,
    val subject: LearningSubject,
    val percent: Int,
    val stars: Int,
    val timestamp: Long,
    val learningLanguage: LearningLanguage? = null
)

data class WeeklyReportEntry(
    val subject: LearningSubject,
    val totalStars: Int,
    val averagePercent: Int,
    val sessionCount: Int,
    val languagesPracticed: Set<LearningLanguage> = emptySet()
)

data class ParentDashboardLevel(
    val id: Long = 0,
    val seed: Long,
    val levelNumber: Int,
    val difficulty: Difficulty,
    val title: String,
    val instruction: String,
    val taskType: DashboardTaskType,
    val steps: List<DashboardStepMode> = listOf(
        DashboardStepMode.INTRO, DashboardStepMode.ACTION, DashboardStepMode.REVIEW
    ),
    val requiredChildCount: Int = 0,
    val requiredLogCount: Int = 0,
    val requiredSubjects: Set<LearningSubject> = emptySet(),
    val isTutorial: Boolean = false,
    val isEndless: Boolean = false,
    val challengeType: ChallengeType? = null,
    val challengeKey: String? = null
) {
    val stepCount: Int get() = steps.size
    companion object {
        const val CAMPAIGN_LEVELS = 15
        const val TUTORIAL_COUNT = 5
    }
}

data class ParentDashboardGame(
    val id: Long = 0,
    val level: ParentDashboardLevel,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val currentStepIndex: Int = 0,
    val children: List<ChildProfile> = emptyList(),
    val selectedChildId: String? = null,
    val progressByChild: Map<String, List<SubjectProgress>> = emptyMap(),
    val progressLog: List<ProgressLogEntry> = emptyList(),
    val weeklyReportViewed: Boolean = false,
    val childrenAddedThisSession: Int = 0,
    val hintsUsed: Int = 0,
    val moves: Int = 0,
    val elapsedSeconds: Long = 0,
    val awaitingAdvance: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val coinsEarned: Int = 0,
    val xpEarned: Int = 0
) {
    val isCompleted: Boolean get() = status == GameStatus.COMPLETED
    val currentStep: DashboardStepMode? get() = level.steps.getOrNull(currentStepIndex)
    val selectedChild: ChildProfile? get() = children.firstOrNull { it.id == selectedChildId }
    val completionTimeFormatted: String get() {
        val mins = elapsedSeconds / 60
        val secs = elapsedSeconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

data class UserStats(
    val gamesPlayed: Int = 0, val gamesWon: Int = 0, val gamesAbandoned: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val fastestTimeBeginner: Long = Long.MAX_VALUE, val fastestTimeEasy: Long = Long.MAX_VALUE,
    val fastestTimeMedium: Long = Long.MAX_VALUE, val fastestTimeHard: Long = Long.MAX_VALUE,
    val fastestTimeExpert: Long = Long.MAX_VALUE, val fastestTimeMaster: Long = Long.MAX_VALUE,
    val currentStreak: Int = 0, val longestStreak: Int = 0, val lastPlayedDate: String = "",
    val xpPoints: Long = 0, val level: Int = 1, val hintsUsedTotal: Int = 0,
    val perfectGames: Int = 0, val poursTotal: Int = 0, val endlessHighScore: Int = 0
) {
    val winRate: Float get() = if (gamesPlayed > 0) gamesWon.toFloat() / gamesPlayed else 0f
    val rank: PlayerRank get() = PlayerRank.fromLevel(level)
}

data class Achievement(
    val id: String, val titleRes: Int, val descriptionRes: Int, val iconName: String,
    val xpReward: Int, val coinReward: Int = 0, val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null, val progress: Int = 0, val maxProgress: Int = 1
)

data class ChallengeRecord(
    val key: String, val type: ChallengeType, val seed: Long, val difficulty: Difficulty,
    val isCompleted: Boolean = false, val completionTime: Long? = null, val moves: Int = 0,
    val rewardCoins: Int = 0, val rewardXp: Int = 0, val streakDay: Int = 0
)

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM, val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = true, val reducedMotion: Boolean = false,
    val highContrastMode: Boolean = false, val colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    val fontScale: Float = 1.0f, val timerVisible: Boolean = true, val adsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false, val consentGiven: Boolean = false,
    val analyticsEnabled: Boolean = true, val personalizedAds: Boolean = false,
    val language: String = "system",
    val defaultLearningLanguage: LearningLanguage = LearningLanguage.ENGLISH,
    val unlockedThemes: Set<String> = setOf(AppTheme.SYSTEM.name, AppTheme.LIGHT.name, AppTheme.DARK.name)
)

data class EconomyState(
    val coins: Int = 100, val totalCoinsEarned: Int = 100, val totalCoinsSpent: Int = 0,
    val unlockableThemes: List<String> = listOf(
        AppTheme.AMOLED.name, AppTheme.NEON.name, AppTheme.CYBER.name,
        AppTheme.SPACE.name, AppTheme.NATURE.name
    ),
    val unlockedThemeIds: Set<String> = emptySet()
)

data class MultiplayerSession(
    val mode: MultiplayerMode, val localPlayerName: String,
    val remotePlayerName: String? = null, val activePlayerName: String = localPlayerName,
    val localScore: Int = 0, val remoteScore: Int = 0, val isActive: Boolean = false,
    val seed: Long = 0L, val difficulty: Difficulty = Difficulty.EASY
)
