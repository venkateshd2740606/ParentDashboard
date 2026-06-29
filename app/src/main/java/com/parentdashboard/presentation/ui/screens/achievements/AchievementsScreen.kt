package com.parentdashboard.presentation.ui.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.domain.model.Achievement
import com.parentdashboard.presentation.viewmodel.AchievementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onNavigateBack: () -> Unit, viewModel: AchievementsViewModel = hiltViewModel()) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.achievements)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(achievements) { achievement -> AchievementCard(achievement) }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (achievement.isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                contentDescription = null,
                tint = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(achievement.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(achievement.descriptionRes), style = MaterialTheme.typography.bodySmall)
                if (achievement.maxProgress > 1) {
                    LinearProgressIndicator(
                        progress = { achievement.progress.toFloat() / achievement.maxProgress },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }
            Text(stringResource(R.string.achievement_xp_bonus, achievement.xpReward))
        }
    }
}
