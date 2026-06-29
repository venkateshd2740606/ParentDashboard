package com.parentdashboard.presentation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.presentation.ui.util.localizedDescription
import com.parentdashboard.presentation.ui.util.localizedLabel
import com.parentdashboard.presentation.ui.util.localizedTitle
import com.parentdashboard.presentation.viewmodel.PuzzleProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: PuzzleProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.puzzle_profile)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (profile.metrics.gamesAnalyzed == 0) {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.profile_empty_message),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.you_are), style = MaterialTheme.typography.labelLarge)
                        Text(profile.archetype.localizedTitle(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(profile.archetype.localizedDescription())
                        Text(
                            stringResource(
                                R.string.profile_games_analyzed,
                                profile.metrics.gamesAnalyzed
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                ProfileInsightCard(
                    title = stringResource(R.string.profile_strength),
                    category = profile.strength.localizedLabel(),
                    detail = stringResource(R.string.profile_percentile_top, viewModel.strengthTopPercent(profile))
                )
                ProfileInsightCard(
                    title = stringResource(R.string.profile_weakness),
                    category = profile.weakness.localizedLabel(),
                    detail = if (viewModel.weaknessNeedsPractice(profile)) {
                        stringResource(R.string.profile_needs_practice)
                    } else {
                        stringResource(R.string.profile_percentile_top, viewModel.weaknessTopPercent(profile))
                    }
                )

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.adaptive_difficulty), fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.adaptive_difficulty_desc))
                        Text(
                            when (profile.adaptiveColorModifier) {
                                -1 -> stringResource(R.string.adaptive_easier)
                                0 -> stringResource(R.string.adaptive_balanced)
                                else -> stringResource(R.string.adaptive_harder)
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInsightCard(title: String, category: String, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
