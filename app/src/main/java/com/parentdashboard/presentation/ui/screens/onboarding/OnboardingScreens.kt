package com.parentdashboard.presentation.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentdashboard.R
import com.parentdashboard.presentation.ui.util.localizedName
import com.parentdashboard.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(onComplete: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        Triple(Icons.Default.Extension, R.string.onboarding_welcome_title, R.string.onboarding_welcome_desc),
        Triple(Icons.Default.Build, R.string.onboarding_screws_title, R.string.onboarding_screws_desc),
        Triple(Icons.Default.Layers, R.string.onboarding_layers_title, R.string.onboarding_layers_desc)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (icon, titleRes, descRes) = pages[page]
        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(titleRes), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(descRes), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pages.indices.forEach { i ->
                val selected = i == page
                Box(
                    modifier = Modifier.size(if (selected) 12.dp else 8.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        if (page < pages.lastIndex) {
            Button(onClick = { page++ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.next))
            }
        } else {
            Button(onClick = { viewModel.completeOnboarding(onComplete) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.get_started))
            }
        }
        if (page > 0) {
            TextButton(onClick = { page-- }) { Text(stringResource(R.string.back)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(onNavigateBack: () -> Unit, onStartTutorial: (Int) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tutorial)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            com.parentdashboard.engine.TutorialLevels.all.forEachIndexed { index, level ->
                ElevatedCard(onClick = { onStartTutorial(index) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.tutorial_level, index + 1), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.tutorial_level_desc, level.difficulty.localizedName()))
                    }
                }
            }
        }
    }
}
