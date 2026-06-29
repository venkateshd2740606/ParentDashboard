package com.parentdashboard.presentation.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.ads.AdManager
import com.parentdashboard.presentation.ui.components.AdBanner
import com.parentdashboard.presentation.ui.theme.CoinGold
import com.parentdashboard.presentation.ui.theme.StreakGold
import com.parentdashboard.presentation.ui.util.localizedTitle
import com.parentdashboard.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToModeSelect: () -> Unit,
    onNavigateToGame: (Long) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToPuzzleProfile: () -> Unit,
    onNavigateToSeedLab: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToDailyChallenge: () -> Unit,
    onNavigateToWeeklyChallenge: () -> Unit,
    onNavigateToMonthlyChallenge: () -> Unit,
    onNavigateToSameDevice: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    adManager: AdManager,
    adsEnabled: Boolean = true,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold)
                },
                actions = {
                    Text(
                        text = stringResource(R.string.coins_count, state.coins),
                        color = CoinGold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        bottomBar = { AdBanner(adManager, adsEnabled = adsEnabled) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.level_display, state.stats.level), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.rank_display, state.stats.rank.localizedTitle()))
                    if (state.stats.currentStreak > 0) {
                        Text(
                            stringResource(R.string.day_streak, state.stats.currentStreak),
                            color = StreakGold
                        )
                    }
                }
            }

            if (state.puzzleProfile.metrics.gamesAnalyzed > 0) {
                Card(
                    onClick = onNavigateToPuzzleProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.you_are), style = MaterialTheme.typography.labelMedium)
                        Text(
                            state.puzzleProfile.archetype.localizedTitle(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(stringResource(R.string.view_puzzle_profile))
                    }
                }
            }

            state.inProgressGame?.let { game ->
                Button(
                    onClick = { onNavigateToGame(game.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.continue_game))
                }
            }

            HomeMenuButton(Icons.Default.Extension, stringResource(R.string.play_color_sort), onNavigateToModeSelect)
            HomeMenuButton(Icons.Default.Science, stringResource(R.string.seed_lab), onNavigateToSeedLab)
            HomeMenuButton(Icons.Default.Person, stringResource(R.string.puzzle_profile), onNavigateToPuzzleProfile)
            HomeMenuButton(Icons.Default.Today, stringResource(R.string.daily_challenge), onNavigateToDailyChallenge)
            HomeMenuButton(Icons.Default.DateRange, stringResource(R.string.weekly_challenge), onNavigateToWeeklyChallenge)
            HomeMenuButton(Icons.Default.CalendarMonth, stringResource(R.string.monthly_challenge), onNavigateToMonthlyChallenge)
            HomeMenuButton(Icons.Default.People, stringResource(R.string.multiplayer), onNavigateToSameDevice)
            HomeMenuButton(Icons.Default.School, stringResource(R.string.tutorial), onNavigateToTutorial)
            HomeMenuButton(Icons.Default.BarChart, stringResource(R.string.statistics), onNavigateToStats)
            HomeMenuButton(Icons.Default.EmojiEvents, stringResource(R.string.achievements), onNavigateToAchievements)
        }
    }
}

@Composable
private fun HomeMenuButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Icon(icon, contentDescription = label)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
