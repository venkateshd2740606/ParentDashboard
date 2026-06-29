package com.parentdashboard.presentation.ui.screens.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.domain.model.ChallengeType
import com.parentdashboard.presentation.ui.theme.StreakGold
import com.parentdashboard.presentation.ui.util.localizedName
import com.parentdashboard.presentation.viewmodel.ChallengeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    type: ChallengeType,
    titleRes: Int,
    onNavigateBack: () -> Unit,
    onStartChallenge: () -> Unit,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val challenge by viewModel.challenge.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()

    LaunchedEffect(type) { viewModel.load(type) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            challenge?.let { c ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.challenge_difficulty, c.difficulty.localizedName()))
                        Text(stringResource(R.string.reward_coins, c.rewardCoins))
                        Text(stringResource(R.string.reward_xp, c.rewardXp))
                        if (streak > 0) Text(stringResource(R.string.day_streak, streak), color = StreakGold)
                        if (c.isCompleted) {
                            Text(stringResource(R.string.challenge_completed))
                            c.completionTime?.let { Text(stringResource(R.string.best_time, formatTime(it))) }
                        } else {
                            Button(onClick = onStartChallenge, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.start_challenge))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.challenge_history), style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(history) { record ->
                    ListItem(
                        headlineContent = { Text(record.key) },
                        supportingContent = {
                            Text(
                                if (record.isCompleted) stringResource(R.string.completed_with_moves, record.moves)
                                else stringResource(R.string.not_completed)
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
fun DailyChallengeScreen(onNavigateBack: () -> Unit, onStartChallenge: () -> Unit) =
    ChallengeScreen(ChallengeType.DAILY, R.string.daily_challenge, onNavigateBack, onStartChallenge)

@Composable
fun WeeklyChallengeScreen(onNavigateBack: () -> Unit, onStartChallenge: () -> Unit) =
    ChallengeScreen(ChallengeType.WEEKLY, R.string.weekly_challenge, onNavigateBack, onStartChallenge)

@Composable
fun MonthlyChallengeScreen(onNavigateBack: () -> Unit, onStartChallenge: () -> Unit) =
    ChallengeScreen(ChallengeType.MONTHLY, R.string.monthly_challenge, onNavigateBack, onStartChallenge)
