package com.parentdashboard.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.parentdashboard.ads.AdManager
import com.parentdashboard.analytics.AnalyticsManager
import com.parentdashboard.domain.model.ChallengeType
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.UserPreferences
import com.parentdashboard.presentation.ui.screens.achievements.AchievementsScreen
import com.parentdashboard.presentation.ui.screens.challenge.DailyChallengeScreen
import com.parentdashboard.presentation.ui.screens.challenge.MonthlyChallengeScreen
import com.parentdashboard.presentation.ui.screens.challenge.WeeklyChallengeScreen
import com.parentdashboard.presentation.ui.screens.consent.ConsentScreen
import com.parentdashboard.presentation.ui.screens.game.GameScreen
import com.parentdashboard.presentation.ui.screens.home.HomeScreen
import com.parentdashboard.presentation.ui.screens.mode.ModeSelectScreen
import com.parentdashboard.presentation.ui.screens.multiplayer.MultiplayerHubScreen
import com.parentdashboard.presentation.ui.screens.multiplayer.MultiplayerLobbyScreen
import com.parentdashboard.presentation.ui.screens.multiplayer.SameDeviceScreen
import com.parentdashboard.presentation.ui.screens.multiplayer.VsAiScreen
import com.parentdashboard.presentation.ui.screens.onboarding.OnboardingScreen
import com.parentdashboard.presentation.ui.screens.onboarding.TutorialScreen
import com.parentdashboard.presentation.ui.screens.profile.PuzzleProfileScreen
import com.parentdashboard.presentation.ui.screens.seedlab.SeedLabScreen
import com.parentdashboard.presentation.ui.screens.settings.SettingsScreen
import com.parentdashboard.presentation.ui.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    data object Consent : Screen("consent")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object ModeSelect : Screen("mode_select")
    data object Game : Screen(
        "game?gameId={gameId}&difficulty={difficulty}&tutorial={tutorial}&endless={endless}" +
            "&seed={seed}&levelNumber={levelNumber}&challengeType={challengeType}" +
            "&sameDevice={sameDevice}&playerOne={playerOne}&playerTwo={playerTwo}" +
            "&networkP2P={networkP2P}&vsAi={vsAi}"
    ) {
        fun newGame(difficulty: Difficulty) = build(
            gameId = -1L,
            difficulty = difficulty.name,
            tutorial = -1,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = false
        )

        fun challenge(type: ChallengeType) = build(
            gameId = -1L,
            difficulty = Difficulty.MEDIUM.name,
            tutorial = -1,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = type.name,
            sameDevice = false
        )

        fun existing(gameId: Long) = build(
            gameId = gameId,
            difficulty = Difficulty.EASY.name,
            tutorial = -1,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = false
        )

        fun tutorial(index: Int) = build(
            gameId = -1L,
            difficulty = Difficulty.EASY.name,
            tutorial = index,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = false
        )

        fun endless(wave: Int) = build(
            gameId = -1L,
            difficulty = Difficulty.ENDLESS.name,
            tutorial = -1,
            endless = wave,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = false
        )

        fun fromSeed(seed: Long, levelNumber: Int, difficulty: Difficulty) = build(
            gameId = -1L,
            difficulty = difficulty.name,
            tutorial = -1,
            endless = -1,
            seed = seed,
            levelNumber = levelNumber,
            challengeType = "",
            sameDevice = false
        )

        fun sameDevice(playerOne: String, playerTwo: String, difficulty: Difficulty) = build(
            gameId = -1L,
            difficulty = difficulty.name,
            tutorial = -1,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = true,
            playerOne = playerOne,
            playerTwo = playerTwo,
            networkP2P = false,
            vsAi = false
        )

        fun networkP2P(difficulty: Difficulty) = build(
            gameId = -1L,
            difficulty = difficulty.name,
            tutorial = -1,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = false,
            networkP2P = true,
            vsAi = false
        )

        fun vsAi(difficulty: Difficulty) = build(
            gameId = -1L,
            difficulty = difficulty.name,
            tutorial = -1,
            endless = -1,
            seed = -1L,
            levelNumber = 1,
            challengeType = "",
            sameDevice = false,
            networkP2P = false,
            vsAi = true
        )

        private fun build(
            gameId: Long,
            difficulty: String,
            tutorial: Int,
            endless: Int,
            seed: Long,
            levelNumber: Int,
            challengeType: String,
            sameDevice: Boolean,
            networkP2P: Boolean = false,
            vsAi: Boolean = false,
            playerOne: String = "",
            playerTwo: String = ""
        ): String =
            "game?gameId=$gameId&difficulty=$difficulty&tutorial=$tutorial&endless=$endless" +
                "&seed=$seed&levelNumber=$levelNumber&challengeType=$challengeType" +
                "&sameDevice=$sameDevice&playerOne=${Uri.encode(playerOne)}&playerTwo=${Uri.encode(playerTwo)}" +
                "&networkP2P=$networkP2P&vsAi=$vsAi"
    }

    data object Stats : Screen("stats")
    data object PuzzleProfile : Screen("puzzle_profile")
    data object SeedLab : Screen("seed_lab")
    data object Settings : Screen("settings")
    data object Achievements : Screen("achievements")
    data object DailyChallenge : Screen("daily_challenge")
    data object WeeklyChallenge : Screen("weekly_challenge")
    data object MonthlyChallenge : Screen("monthly_challenge")
    data object SameDevice : Screen("same_device")
    data object MultiplayerHub : Screen("multiplayer_hub")
    data object MultiplayerLobby : Screen("multiplayer_lobby")
    data object VsAi : Screen("vs_ai")
    data object Tutorial : Screen("tutorial")
}

@Composable
fun ParentDashboardNavHost(
    navController: NavHostController,
    adManager: AdManager,
    analyticsManager: AnalyticsManager,
    preferences: UserPreferences,
    startDestination: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    DisposableEffect(navBackStackEntry?.destination?.route) {
        navBackStackEntry?.destination?.route?.let { analyticsManager.logScreenView(it) }
        onDispose { }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Consent.route) {
            ConsentScreen(onComplete = {
                navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Consent.route) { inclusive = true } }
            })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToModeSelect = { navController.navigate(Screen.ModeSelect.route) },
                onNavigateToGame = { navController.navigate(Screen.Game.existing(it)) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToPuzzleProfile = { navController.navigate(Screen.PuzzleProfile.route) },
                onNavigateToSeedLab = { navController.navigate(Screen.SeedLab.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) },
                onNavigateToDailyChallenge = { navController.navigate(Screen.DailyChallenge.route) },
                onNavigateToWeeklyChallenge = { navController.navigate(Screen.WeeklyChallenge.route) },
                onNavigateToMonthlyChallenge = { navController.navigate(Screen.MonthlyChallenge.route) },
                onNavigateToSameDevice = { navController.navigate(Screen.MultiplayerHub.route) },
                onNavigateToTutorial = { navController.navigate(Screen.Tutorial.route) },
                adManager = adManager,
                adsEnabled = preferences.adsEnabled
            )
        }
        composable(Screen.ModeSelect.route) {
            ModeSelectScreen(
                onNavigateBack = { navController.navigateUp() },
                onSelectDifficulty = { navController.navigate(Screen.Game.newGame(it)) },
                onSelectEndless = { navController.navigate(Screen.Game.endless(1)) }
            )
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("difficulty") { type = NavType.StringType; defaultValue = "EASY" },
                navArgument("tutorial") { type = NavType.IntType; defaultValue = -1 },
                navArgument("endless") { type = NavType.IntType; defaultValue = -1 },
                navArgument("seed") { type = NavType.LongType; defaultValue = -1L },
                navArgument("levelNumber") { type = NavType.IntType; defaultValue = 1 },
                navArgument("challengeType") { type = NavType.StringType; defaultValue = "" },
                navArgument("sameDevice") { type = NavType.BoolType; defaultValue = false },
                navArgument("playerOne") { type = NavType.StringType; defaultValue = "" },
                navArgument("playerTwo") { type = NavType.StringType; defaultValue = "" },
                navArgument("networkP2P") { type = NavType.BoolType; defaultValue = false },
                navArgument("vsAi") { type = NavType.BoolType; defaultValue = false }
            )
        ) { entry ->
            val gameId = entry.arguments?.getLong("gameId")?.takeIf { it > 0 }
            val difficulty = entry.arguments?.getString("difficulty")?.let {
                runCatching { Difficulty.valueOf(it) }.getOrNull()
            }
            val tutorial = entry.arguments?.getInt("tutorial")?.takeIf { it >= 0 }
            val endless = entry.arguments?.getInt("endless")?.takeIf { it >= 0 }
            val seed = entry.arguments?.getLong("seed")?.takeIf { it >= 0 }
            val levelNumber = entry.arguments?.getInt("levelNumber") ?: 1
            val challengeType = entry.arguments?.getString("challengeType")
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { ChallengeType.valueOf(it) }.getOrNull() }
            val sameDevice = entry.arguments?.getBoolean("sameDevice") ?: false
            val playerOne = entry.arguments?.getString("playerOne").orEmpty()
            val playerTwo = entry.arguments?.getString("playerTwo").orEmpty()
            val networkP2P = entry.arguments?.getBoolean("networkP2P") ?: false
            val vsAi = entry.arguments?.getBoolean("vsAi") ?: false

            GameScreen(
                gameId = gameId,
                difficulty = if (gameId == null && tutorial == null && endless == null && seed == null && challengeType == null && !sameDevice) {
                    difficulty
                } else null,
                tutorialIndex = tutorial,
                endlessWave = endless,
                seed = seed,
                levelNumber = levelNumber,
                challengeType = challengeType,
                sameDevice = sameDevice,
                networkP2P = networkP2P,
                vsAi = vsAi,
                playerOne = playerOne,
                playerTwo = playerTwo,
                hapticFeedback = preferences.hapticFeedback,
                soundEnabled = preferences.soundEnabled,
                timerVisible = preferences.timerVisible,
                onNavigateBack = { navController.navigateUp() },
                adManager = adManager,
                reducedMotion = preferences.reducedMotion
            )
        }
        composable(Screen.Stats.route) { StatsScreen(onNavigateBack = { navController.navigateUp() }) }
        composable(Screen.PuzzleProfile.route) {
            PuzzleProfileScreen(onNavigateBack = { navController.navigateUp() })
        }
        composable(Screen.SeedLab.route) {
            SeedLabScreen(
                onNavigateBack = { navController.navigateUp() },
                onPlaySeed = { seed, levelNumber, difficulty ->
                    navController.navigate(Screen.Game.fromSeed(seed, levelNumber, difficulty))
                }
            )
        }
        composable(Screen.Settings.route) {
            val context = LocalContext.current
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPrivacy = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://parentdashboard.game/privacy")))
                }
            )
        }
        composable(Screen.Achievements.route) { AchievementsScreen(onNavigateBack = { navController.navigateUp() }) }
        composable(Screen.DailyChallenge.route) {
            DailyChallengeScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartChallenge = { navController.navigate(Screen.Game.challenge(ChallengeType.DAILY)) }
            )
        }
        composable(Screen.WeeklyChallenge.route) {
            WeeklyChallengeScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartChallenge = { navController.navigate(Screen.Game.challenge(ChallengeType.WEEKLY)) }
            )
        }
        composable(Screen.MonthlyChallenge.route) {
            MonthlyChallengeScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartChallenge = { navController.navigate(Screen.Game.challenge(ChallengeType.MONTHLY)) }
            )
        }
        composable(Screen.MultiplayerHub.route) {
            MultiplayerHubScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSameDevice = { navController.navigate(Screen.SameDevice.route) },
                onNavigateToTwoDevice = { navController.navigate(Screen.MultiplayerLobby.route) },
                onNavigateToVsAi = { navController.navigate(Screen.VsAi.route) }
            )
        }
        composable(Screen.MultiplayerLobby.route) {
            MultiplayerLobbyScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartGame = { difficulty ->
                    navController.navigate(Screen.Game.networkP2P(difficulty))
                }
            )
        }
        composable(Screen.VsAi.route) {
            VsAiScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartGame = { difficulty ->
                    navController.navigate(Screen.Game.vsAi(difficulty))
                }
            )
        }
        composable(Screen.SameDevice.route) {
            SameDeviceScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartGame = { p1, p2, difficulty ->
                    navController.navigate(Screen.Game.sameDevice(p1, p2, difficulty))
                }
            )
        }
        composable(Screen.Tutorial.route) {
            TutorialScreen(
                onNavigateBack = { navController.navigateUp() },
                onStartTutorial = { navController.navigate(Screen.Game.tutorial(it)) }
            )
        }
    }
}
