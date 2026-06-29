package com.parentdashboard.presentation.ui.screens.mode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.presentation.viewmodel.ModeSelectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectScreen(
    onNavigateBack: () -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
    onSelectEndless: () -> Unit,
    viewModel: ModeSelectViewModel = hiltViewModel()
) {
    val campaignLevels by viewModel.campaignLevels.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.choose_difficulty)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Difficulty.entries.filter { it != Difficulty.ENDLESS }) { difficulty ->
                val label = when (difficulty) {
                    Difficulty.BEGINNER -> stringResource(R.string.difficulty_beginner)
                    Difficulty.EASY -> stringResource(R.string.difficulty_easy)
                    Difficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
                    Difficulty.HARD -> stringResource(R.string.difficulty_hard)
                    Difficulty.EXPERT -> stringResource(R.string.difficulty_expert)
                    Difficulty.MASTER -> stringResource(R.string.difficulty_master)
                    Difficulty.ENDLESS -> stringResource(R.string.endless_mode)
                }
                val level = campaignLevels[difficulty] ?: 1
                ElevatedCard(
                    onClick = { onSelectDifficulty(difficulty) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(label, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.level_display, level),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            repeat(difficulty.stars) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            item {
                ElevatedCard(onClick = onSelectEndless, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.endless_mode),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
