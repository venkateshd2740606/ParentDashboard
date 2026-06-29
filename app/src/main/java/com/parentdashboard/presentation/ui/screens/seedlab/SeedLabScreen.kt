package com.parentdashboard.presentation.ui.screens.seedlab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentdashboard.R
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.presentation.ui.util.localizedName
import com.parentdashboard.presentation.viewmodel.SeedLabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedLabScreen(
    onNavigateBack: () -> Unit,
    onPlaySeed: (seed: Long, levelNumber: Int, difficulty: Difficulty) -> Unit,
    viewModel: SeedLabViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var seedInput by remember { mutableStateOf("") }
    var levelInput by remember { mutableStateOf("1") }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seed_lab)) },
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
            Text(stringResource(R.string.seed_lab_desc))

            OutlinedTextField(
                value = seedInput,
                onValueChange = { seedInput = it.filter { ch -> ch.isDigit() || ch == '-' } },
                label = { Text(stringResource(R.string.level_seed)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = levelInput,
                onValueChange = { levelInput = it.filter { ch -> ch.isDigit() } },
                label = { Text(stringResource(R.string.level_number)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(stringResource(R.string.choose_difficulty))
            Difficulty.entries.filter { it != Difficulty.ENDLESS }.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { selectedDifficulty = difficulty },
                    label = { Text(difficulty.localizedName()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val seed = seedInput.toLongOrNull()
                    val levelNumber = levelInput.toIntOrNull()
                    when {
                        seed == null -> errorMessage = context.getString(R.string.invalid_seed)
                        levelNumber == null || levelNumber < 1 -> errorMessage = context.getString(R.string.invalid_level_number)
                        else -> {
                            errorMessage = null
                            onPlaySeed(seed, levelNumber, selectedDifficulty)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.play_seed))
            }

            OutlinedButton(
                onClick = {
                    val sample = viewModel.generateSampleSeed()
                    seedInput = sample.seed.toString()
                    levelInput = sample.levelNumber.toString()
                    selectedDifficulty = sample.difficulty
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generate_sample_seed))
            }
        }
    }
}
